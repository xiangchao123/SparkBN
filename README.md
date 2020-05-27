# SparkBN
基于Spark的K2算法

spark-submit --class spark.SimpleApp --driver-memory 3g --conf "spark.driver.memoryOverhead=5g" --conf "spark.driver.extraJavaOptions=-XX:MaxDirectMemorySize=3g" --master yarn  --executor-memory 3g --conf "spark.executor.memoryOverhead=5G" --conf "spark.executor.extraJavaOptions=-XX:MaxDirectMemorySize=3g" --num-executors 3 --executor-cores 6  simple-project-1.0.jar
