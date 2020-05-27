# SparkBN
基于Spark的K2算法

spark-submit --class spark.SimpleApp --driver-memory 8g --conf "spark.driver.memoryOverhead=2g" --conf "spark.driver.extraJavaOptions=-XX:MaxDirectMemorySize=1g" --master yarn  --executor-memory 8g --conf "spark.executor.memoryOverhead=2G" --conf "spark.executor.extraJavaOptions=-XX:MaxDirectMemorySize=1g" --num-executors 3 --executor-cores 6  simple-project-1.0.jar
