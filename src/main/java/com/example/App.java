package com.example;

import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.avro.generic.GenericRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class App 
{
    public static void main(String[] args) {
        // Check if the file path argument is provided
        if (args.length < 1) {
            System.err.println("Usage: java -cp <your-jar>.jar com.example.App <path-to-parquet-file>");
            System.exit(1);
        }

        String filePath = args[0];
        Path path = new Path(filePath);
        List<Double> distances = new ArrayList<>();

        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(path).withConf(new Configuration()).build()) {
            GenericRecord trip;

            // Extract the distances based on the NYC Yellow Taxi Trip Record Schema
            while ((trip = reader.read()) != null) {
                Double tripDistance = (Double) trip.get("trip_distance");
                if (tripDistance != null) {
                    distances.add(tripDistance);
                }
            }

            // Sort the distances for an accurate percentile calculation
            Collections.sort(distances);

            // Convert the sorted list to an array
            double[] distanceArray = distances.stream().mapToDouble(Double::doubleValue).toArray();

            // Calculate the 90th percentile distance
            Percentile percentile = new Percentile();
            double threshold = percentile.evaluate(distanceArray, 90.0);

			//Indicate the calculated distance threshold for the trips.
            System.out.println("90th Percentile Distance: " + threshold);

            // Filter and print trips with distance in the 90th percentile
            try (ParquetReader<GenericRecord> filterTrips = AvroParquetReader.<GenericRecord>builder(path).withConf(new Configuration()).build()) {
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
