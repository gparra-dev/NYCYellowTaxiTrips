# NYCYellowTaxiTrips

Hello! This is a very basic app which receives the path to a NYC Yellow Trips Parquet file
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
	* for like this echo "
5. I typically run the app from the NYCYellowTaxiTrips folder with the following command:
	* `java -cp target/nyctaxitrip-reader-1.0-SNAPSHOT.jar com.example.App ../yellow_tripdata_2024-06.parquet > ../output.txt`
	* or in generic form:
	* `java -cp <PATHTOJAR>/nyctaxitrip-reader-1.0-SNAPSHOT.jar com.example.App <PATHTOParquetFile>/<parquetfilename> > <PATHTOOUTPUTFILE>/<outputfilename>`
6. The app should run in your local machine or server you are running it on.
	* Should take a minute or less or more depending on your system	to process the large file.
7. The output file looks like this:
	* First row: `90th Percentile Distance: 8.73`
	* all other rows: <EACH TRIP AS A ONE LINE JSON OBJECT>

## Potential Issues and Performance Considerations
1. This is a fairly basic app:
	* Loads all distances in memory to calculate the 90th percentile
	* Fortunately the Parquet format makes that not too heavy on the memory for these files.
	* No parallelization, it reads all distances sequentially and prints the filtered records sequentially
2. If you have a system with very low resources and low memory, this program may not be able to run
	* The entire app with the source code and compiled files is ~141MB in size.
	* Based on my rough estimates you may need ~1GB to 1.3GB of memory to process a file

## How could this app be improved to tackle those issues
1. Introducing parallelization could improve speed and reduce peak memory consumption
	* Could chunk the file in pieces and load the distances
	* Could chunk the file in chunks and filter each trip based on percentile through many chunks in parallel
2. Could have used Apache Spark to do distributed computing
	* This would leverage parallelization
	* And would lower the memory requirements
3. Could a streaming percentile calculation
	* avoid loading all distances in memory to calculate

## What's next?
1. In all honesty, this was built after a long time not building an app from scratch
2. It's raw but it accomplishes it's goal effectively
3. I tried 1 and 2 above but:
	* I'm going to need to dig deeper into dependencies and parallelization in Java
	* For that I'd need a bit more time than the few hours I've put into this so far.
	* It's possible, but this is the first task, will focus on more improvements if necessary.


