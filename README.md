# SparkBN
基于Spark的K2算法

spark-submit --class spark.SimpleApp --driver-memory 3g --master yarn --deploy-mode cluster --executor-memory 3g --conf "spark.yarn.executor.memoryOverhead=6G" --total-executor-cores 18 simple-project-1.0.jar



spark-submit --class spark.SimpleApp --driver-memory 8g --conf "spark.driver.memoryOverhead=3g" --master yarn  --conf "spark.executor.memory=9G" --conf "spark.executor.memoryOverhead=4G" --num-executors 3 --executor-cores 6   simple-project-1.0.jar

