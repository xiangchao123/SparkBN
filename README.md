# SparkBN
基于Spark的K2算法

spark-submit --class spark.SimpleApp --driver-memory 3g --master yarn --deploy-mode cluster --executor-memory 3g --conf "spark.yarn.executor.memoryOverhead=6G" --total-executor-cores 18 simple-project-1.0.jar

