# NYCYellowTaxiTrips V3

New in V3! Using a projection of the schema to deserialize the file
See more in the section "Potential Issues and Performance Considerations" of the readme.

Hello! This is an app which receives the path to a NYC Yellow Trips Parquet file
as an argument, and then processes the file to look for all trips within the highest 90th percentile
based on the trip_distance.

You can obtain the parquet files here: [TLC Trip Record Data](https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page)

This program is written in JAVA and uses Maven to handle dependencies.
It leverages Apache Parquet and Apache Arrow to process the files.

## What you need to compile and run the app
1. JAVA installed in your system (I used JDK 17 for this) and added to your PATH environment variable
	* you can download and find installation instructions here: [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
	
2. MAVEN installed in your system and added to your PATH environment variable
	* you can download Maven here: [Maven Download](https://maven.apache.org/download.cgi)
	* To install you just need to extract the archive and add the bin and mvn command to your PATH
	* [Installation Instructions](https://maven.apache.org/install.html)
	
## How to compile the app
1. Clone the project from github
	* `git clone https://github.com/gparra-dev/NYCYellowTaxiTrips.git`
2. Once cloned, go into the NYCYellowTaxiTrips directory and run the following command:
	* `mvn package`
	* you should see a lot of messages and warnings but no errors.
	* once the compilation is complete you will see a new `target` directory
	* In it you will see the the JAR you will use to run the app `nyctaxitrip-reader-1.0-SNAPSHOT.jar`
	
## How to run the app
1. Make sure you have downloaded one of the Parquet files with yellow cab trips.
2. Place the file somewhere easy to handle, i like placing it in the parent folder of NYCYellowTaxiTrips
3. As an example I will use the file `yellow_tripdata_2024-06.parquet`
4. The app will print it's results on screen, so you'll want to output the results to a file.
	* in windows git bash or in linux you can use the operator `>`
	* like this for example: `echo "Hello World" > helloworld.txt` 
5. I typically run the app from the NYCYellowTaxiTrips folder with the following command:
	* `java -cp target/nyctaxitrip-reader-1.0-SNAPSHOT.jar com.example.App ../yellow_tripdata_2024-06.parquet > ../output.txt`
	* or in generic form:
	* `java -cp <PATHTOJAR>/nyctaxitrip-reader-1.0-SNAPSHOT.jar com.example.App <PATHTOParquetFile>/<parquetfilename> > <PATHTOOUTPUTFILE>/<outputfilename>`
6. The app should run in your local machine or server you are running it on.
	* Should take a minute or less or more depending on your system	to process the large file.
7. The output file in v2 looks like this:
	* First row: `Total number of trips in the Parquet file: 3539193`
	* Second row: `Approximate number of trips in the 90th percentile: 353919`
	* First row: `90th Percentile Distance: 8.73`
	* all other rows: `{"VendorID": 1, "tpep_pickup_datetime": 1717200226000000, "trip_distance": 12.5}`

## Potential Issues and Performance Considerations

1. V3 has made further improvements to speed
	* Before the reader of the file was deserializing all of the fields on each row
	* By defining the schema of what i want to read I can leverage the benefits of the parquet format
	* So when reading the distances to calculate the percentile, now I only deserialize the trip_distance fields
	* When reading the file to filter only the trips in the 90th percentile I took the liberty to only get 3 fields
	* VendorID, trip pickup date, trip distance. Which changes the output of the file significantly
	* With these changes the system takes now about 1/4th of the time it took to process a file (<20 seconds)
2. V2 has made some improvements:
	* Instead of loading all distances in memory, we now obtain the total number of trips from the files metadata
	* Then we calculate the approximate number of trips that would be in the 90th percentile (i.e. totalTrips*0.1)
	* Then we create a minHeap of trip distances and load the approximate number of trips we expect to fit
	* Once we've loaded the expected number of trip distances, we keep reading the trip distances
	* And we replace the first item in the heap (with any distance bigger than it)
	* So we are only loading a 10th of the distances into memory
	* And when we are done the firs item in the minHeap is our threshold to filter trips by
	* Still, No parallelization, it reads distances sequentially and prints the filtered records sequentially
3. If you have a system with very low resources and low memory, this program may not be able to run
	* The entire app with the source code and compiled files is ~141MB in size.
	* Based on my rough estimates you now need about half the memory we needed before to run
	* That's approximately ~500MB to ~800MB of memory to process a file

## How could this app be improved to tackle those issues
1. Introducing parallelization could improve speed and reduce peak memory consumption
	* Could chunk the file in pieces and load the distances into a heap in parallel threads
	* Could chunk the file in pieces and filter each trip based on percentile through many chunks in parallel threads
2. Could have used Apache Spark to do distributed computing
	* This would leverage parallelization
	* And would lower the memory requirements

## What's next?
1. In all honesty, this was built after a long time not building an app from scratch
2. It's raw but it accomplishes it's goal effectively
3. I tried 1 and 2 above but:
	* I'm going to need to dig deeper into dependencies and parallelization in Java
	* For that I still need a bit more time than the few hours I've put into this so far.
	* Happy to do it, but this is the 3rd version. Will focus on more improvements in a v4 if necessary.
	* Hopefully with a little help :).


