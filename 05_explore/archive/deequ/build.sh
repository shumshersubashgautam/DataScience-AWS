./scala-2.11.12/bin/scalac -classpath ./json4s-jackson_2.11-3.6.6.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-network-shuffle_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-repl_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-catalyst_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-sql_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-core_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-launcher_2.11-2.4.5.jar:./spark-2.4.5-bin-hadoop2.7/jars/spark-tags_2.11-2.4.5.jar:./deequ-1.0.2.jar preprocess-deequ.scala -d preprocess-deequ.jar

cp preprocess-deequ.jar ../container/jars/

echo "Copied to preprocess-deequ.jar to ../container/jars/   Don't forget to rebuild the Docker image (currently through the notebook)!"
