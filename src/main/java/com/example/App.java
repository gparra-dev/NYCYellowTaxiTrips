package com.example;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.avro.AvroReadSupport;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.LogicalTypes;

import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import java.util.PriorityQueue;
import java.util.Collections;

public class App 
{
    public static void main(String[] args) {
        // Check if the file path argument is provided
        if (args.length < 1) {
            System.err.println("Usage: java -cp <your-jar>.jar com.example.App <path-to-parquet-file>");
            System.exit(1);
        }

        String filePath = args[0];
		Configuration configuration = new Configuration();
        Path path = new Path(filePath);
		
		ParquetFileReader fileReader = null;
		
		Schema tripSchema = SchemaBuilder.record("TripDistance")
			.fields()
			.optionalDouble("trip_distance")
			.endRecord();
		
		//Get file metadata
		try{
			
			// Open the Parquet file
            HadoopInputFile hadoopInputFile = HadoopInputFile.fromPath(path, configuration);
            fileReader = ParquetFileReader.open(hadoopInputFile);
			
            // Get the file metadata (footer)
            ParquetMetadata metadata = fileReader.getFooter();

            // Initialize a variable to accumulate the total number of trips
            long totalNumberOfTrips = 0;

            // Iterate over each row group and sum up the row counts
            for (BlockMetaData block : metadata.getBlocks()) {
                totalNumberOfTrips += block.getRowCount();
            }

            // Print the total number of trips
            System.out.println("Total number of trips in the Parquet file: " + totalNumberOfTrips);
			
			//Create a minHeap to store the 90th percentile distances
			PriorityQueue<Double> minHeap = new PriorityQueue<>();
			
			//Configure to only read trip_distance
			configuration.set(AvroReadSupport.AVRO_REQUESTED_PROJECTION, tripSchema.toString());

			//Create a reader for the parquet file of trips
			ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(path).withConf(configuration).build();			

			//Create a record to read the trips into
			GenericRecord trip;
			
			//Calculate the number of items in the 90th percentile
			int heapSize = (int)(Math.round(totalNumberOfTrips * 0.1));
			System.out.println("Approximate number of trips in the 90th percentile: "+heapSize);
			
			int heapCount = 0;
            //Extract the distances based on the NYC Yellow Taxi Trip Record Schema
			//Only add the 90th percentile distances to the minHeap
            while ((trip = reader.read()) != null) {
                Double tripDistance = (Double) trip.get("trip_distance");
				
				//Add the first "heapSize" items to the minHeap
                if (tripDistance != null && heapCount <= heapSize ) {
                    minHeap.add(tripDistance);
					heapCount++;
                }
				//For the remaining trips check if the new distance is higher than
				//the first item in the minHeap and replace it if so.
				if (tripDistance !=null && heapCount > heapSize){
					if (tripDistance > minHeap.peek()){
						minHeap.poll();
						minHeap.add(tripDistance);
					}
				}
            }			
			
			//Once we've streamed through all the distances identify the 90th percentile and print it
			System.out.println("90th percentile distance: "+minHeap.peek());
			//Save it to a double
			double threshold = minHeap.peek();
			
			tripSchema = SchemaBuilder.record("BriefTrip")
				.fields()
				.optionalInt("VendorID")
				.name("tpep_pickup_datetime")
                .type().unionOf().nullType().and().type(LogicalTypes.timestampMicros().addToSchema(Schema.create(Schema.Type.LONG))).endUnion().nullDefault()
				.optionalDouble("trip_distance")
				.endRecord();			

			//Configure to only read VendorID, tpep_pickup_datetime, and trip_distance
			configuration.set(AvroReadSupport.AVRO_REQUESTED_PROJECTION, tripSchema.toString());
			
			// Filter and print trips with distance in the 90th percentile
            try (ParquetReader<GenericRecord> filterTrips = AvroParquetReader.<GenericRecord>builder(path).withConf(configuration).build()) {
                while ((trip = filterTrips.read()) != null) {
                    Double tripDistance = (Double) trip.get("trip_distance");
                    if (tripDistance != null && tripDistance >= threshold) {
                        System.out.println(trip);
                    }
                }
            }
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
}
