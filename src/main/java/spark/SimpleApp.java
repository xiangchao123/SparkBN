package spark;


import com.sun.org.apache.regexp.internal.RE;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;

import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.IntegerLiteral;
import scala.Tuple2;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SimpleApp {
    static String text = "/hadoop/test.txt";
//    static String text = "D:\\spark\\src\\main\\java\\test.txt";
    static ArrayList<Integer> order;


    static {
        try {
            order = getOrder(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SimpleApp() throws IOException {
    }

    public static void main(String[] args) throws IOException {
//        String logFile = "D:\\spark\\src\\main\\java\\case.csv";
        String logFile = "/hadoop/case.csv"; // Should be some file on your system
        SparkConf sparkConf = new SparkConf().setAppName("Simple Application");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

//        SparkSession spark = SparkSession.builder()
////                .config("spark.testing.memory", "1024000000")
////                .master("local[*]")
//                .appName("Simple Application").getOrCreate();
        List<List<Integer>> arrayLists = SimpleApp.loadDataSet(logFile);
        int node = arrayLists.get(0).size();
        JavaRDD<Result>[] datasets = new JavaRDD[1];
        int rddindex=0;
        for(int i=0;i<20;){
            int num=0;
            List<Tuple2<Integer,Matrix>> list = new ArrayList<>(20);
            List<Integer> indexlist = new ArrayList<>(20);
            while(num<20){
                Matrix matrix = new Matrix();
                matrix.setArrayLists(arrayLists);
                matrix.setInteger(i);
                list.add(new Tuple2<>(i,matrix));
                indexlist.add(i);
                i++;
                num++;
            }
            JavaPairRDD<Integer, Matrix> integerMatrixJavaPairRDD = sc.parallelizePairs(list, 20);
//            Encoder<Matrix> encoder = Encoders.bean(Matrix.class);
//            Encoder<Result> resultEncoder = Encoders.bean(Result.class);

//            System.out.println("==========================进入");

//            Dataset<Matrix> dataset = spark.createDataset(Collections.singletonList(new Matrix()),encoder).cache();

//            System.out.println("======================================出来");
//            JavaPairRDD<Integer, Matrix> pairRDD = dataset.javaRDD().flatMapToPair(new PairFlatMapFunction<Matrix, Integer, Matrix>()
//            {
//
//                @Override
//                public Iterator<Tuple2<Integer, Matrix>> call(Matrix matrix) throws Exception {
//                    List<Tuple2<Integer, Matrix>> list = new ArrayList<Tuple2<Integer, Matrix>>();
//                    list.add(new Tuple2<>(matrix.getInteger(),matrix));
//                    return list.iterator();
//                }
//            });

            MyPartitioner myPartitioner = new MyPartitioner();
            myPartitioner.setPartitions(indexlist.size());
            myPartitioner.initMap(myPartitioner.getPartitions(),indexlist);
//            JavaRDD<Result> resultJavaRDD = pairRDD.partitionBy(myPartitioner).mapPartitions(new FlatMapFunction<Iterator<Tuple2<Integer, Matrix>>, Result>() {
            JavaRDD<Result> resultJavaRDD = integerMatrixJavaPairRDD.partitionBy(myPartitioner).mapPartitions(new FlatMapFunction<Iterator<Tuple2<Integer, Matrix>>, Result>() {
                private int UPPER_BOUND = 2;
                @Override
                public Iterator<Result> call(Iterator<Tuple2<Integer, Matrix>> tuple2Iterator) throws Exception {
                    List<Result> resultList=new ArrayList<>();
                    while (tuple2Iterator.hasNext()) {
                        Tuple2<Integer, Matrix> tuple2 = tuple2Iterator.next();
                        Result result = getBNStructure(tuple2._2, tuple2._1);
                        resultList.add(result);
                    }
                    return resultList.iterator();
                }

                private Result getBNStructure(Matrix matrix, int index) {
                    Result result = new Result();
                    ArrayList<Integer> parentList = new ArrayList<>();
                    double probOld = calIsParentProb(parentList, order.get(index),matrix);
                    boolean OkToProceed = true;
                    int optimalParent = 0;
                    double probNew = toLog(0);
                    while (OkToProceed && parentList.size() < UPPER_BOUND) {
                        for (int j = 0; j < index; j++) {
                            if (!parentList.contains(order.get(j))) {
                                parentList.add(order.get(j));
                                double currentProb = calIsParentProb(parentList, order.get(index),matrix);
                                if (currentProb > probNew) {
                                    probNew = currentProb;
                                    optimalParent = order.get(j);
                                }
                                parentList.remove(parentList.size() - 1);
                            }
                        }
                        if (probOld < probNew) {
                            probOld = probNew;
                            parentList.add(optimalParent);
                        } else {
                            OkToProceed = false;
                        }
                    }
                    result.setSon(order.get(index));
                    StringBuilder stringBuilder = new StringBuilder();
                    for(Integer integer:parentList){
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                    }
                    result.setParents(stringBuilder.toString());
                    return result;
                }

                private double calIsParentProb(ArrayList<Integer> parentList,
                                               int currentAttr,Matrix matrix) {
                    // list of all possible instantiations of the parents of xi
                    // in database D
                    // int q = parentList.size() * 2;
                    int r = 2;
                    double f = 0.0;
                    int NCount = (int) Math.pow(r, parentList.size());
                    BigIntegerArr bigInteger = new BigIntegerArr();
                    HashMap<Integer, Integer> alphaMap = getAlpha(currentAttr, parentList,
                            r,matrix);
                    for (int i = 0; i < NCount * r; i++) {
                        if (!alphaMap.containsKey(i)) {
                            alphaMap.put(i, 0);
                        }
                    }
                    ArrayList<Integer> nList = getN(alphaMap, NCount);

                    for (int k = 0; k < NCount * r; k++) {
                        // bigFactorial计算阶乘
                        f += toLogBig(bigInteger.bigFactorial((alphaMap.get(k))));
                    }
                    for (int j = 0; j < NCount; j++) {
                        f += (toLogBig(bigInteger.bigFactorial(r - 1))
                                - toLogBig(bigInteger.bigFactorial(nList.get(j) + r - 1)));
                    }
                    return f;
                }

                // 将字符转的结果转换为以10为底的对数结果
                private Double toLogBig(String intStr) {
                    // String intStr = "";
                    String deciStr;
                    int len = intStr.indexOf(".");
                    if (len == 0 || len == intStr.length() - 1) {
                        return 0.0;
                    } else if (len == -1) {
                        deciStr = "0";
                    } else {
                        deciStr = intStr.substring(len + 1, intStr.length());
                        intStr = intStr.substring(1, len);
                    }
                    len = intStr.length();
                    while (len > 1) {
                        if (intStr.startsWith("0")) {
                            intStr = intStr.substring(1, len);
                            len -= 1;
                        } else {
                            break;
                        }
                    }
                    len = deciStr.length();
                    while (len > 1) {
                        if (deciStr.endsWith("0")) {
                            deciStr = deciStr.substring(0, len - 1);
                            len -= 1;
                        } else {
                            break;
                        }
                    }
                    double ret;
                    len = intStr.length();
                    if (len < 16) {
                        if (intStr.length() > 4 || deciStr.length() < 9) {
                            intStr += "." + deciStr;
                            ret = Double.valueOf(intStr);
                            if (ret == 0) {
                                return 0.0;
                            }
                            ret = Math.log10(ret);
                        } else {
                            len = deciStr.length();
                            intStr = intStr + deciStr.substring(0, len - 8);
                            deciStr = deciStr.substring(len - 8, len);
                            ret = 8 - len;
                            len = intStr.length();
                            while (len > 1) {
                                if (intStr.startsWith("0")) {
                                    intStr = intStr.substring(1, intStr.length());
                                } else {
                                    break;
                                }
                            }
                            len = intStr.length();
                            if (len > 9) {
                                len -= 8;
                                deciStr = intStr.substring(8, len + 8) + deciStr;
                                intStr = intStr.substring(0, 8);
                                ret += len;
                            }
                            intStr += "." + deciStr;
                            ret += Math.log10(Double.valueOf(intStr));
                        }
                    } else {
                        len = intStr.length();
                        deciStr = intStr.substring(9, len) + deciStr;
                        intStr = intStr.substring(0, 9) + "." + deciStr;
                        len -= 9;
                        ret = Math.log10(Double.valueOf(intStr)) + Double.valueOf(len);
                    }
                    return ret;
                }

                private ArrayList<Integer> getN(HashMap<Integer, Integer> alphaMap,
                                                int nCount) {
                    ArrayList<Integer> nList = new ArrayList<>();
                    int sum = 0;
                    for (int i = 1; i <= alphaMap.size(); i++) {
                        if (i % (alphaMap.size() / nCount) == 0) {
                            sum += alphaMap.get(i - 1);
                            nList.add(sum);
                            sum = 0;
                        } else {
                            sum += alphaMap.get(i - 1);
                        }

                    }
                    return nList;
                }

                private HashMap<Integer, Integer> getAlpha(int currentAttr,
                                                           ArrayList<Integer> parentList, int r,Matrix matrix) {
                    int nodeNum = parentList.size() + 1;
                    HashMap<Integer, Integer> alphaMap = new HashMap<>();
                    for (List<Integer> dataItem : matrix.getArrayLists()) {
                        boolean okToProceed = true;
                        for (int i = 0; i < Math.pow(r, nodeNum); i++) {
                            String strTemp = Integer.toBinaryString(i);
                            char[] str = getfixedLenStr(strTemp, nodeNum);

                            for (int j = 0; j < parentList.size(); j++) {
                                if (!dataItem.get(parentList.get(j)).equals(str[j] - '0')) {
                                    okToProceed = false;
                                    break;
                                }
                                okToProceed = true;
                            }
                            if (!okToProceed) {
                                continue;
                            }

                            if (!dataItem.get(currentAttr)
                                    .equals(str[str.length - 1] - '0')) {
                                continue;
                            }

                            if (alphaMap.containsKey(i)) {
                                alphaMap.put(i, alphaMap.get(i) + 1);
                            } else {
                                alphaMap.put(i, 1);
                            }
                        }
                    }
                    return alphaMap;
                }

                private char[] getfixedLenStr(String strTemp, int nodeNum) {
                    char[] str = new char[nodeNum];
                    int j = 1;
                    int i = nodeNum - 1;
                    for (; i >= 0; i--) {
                        str[i] = strTemp.charAt(strTemp.length() - j);
                        j++;
                        if (j > strTemp.length()) {
                            break;
                        }
                    }
                    i--;
                    while (i >= 0) {
                        str[i] = '0';
                        i--;
                    }
                    return str;
                }

                // 转换成对数
                private double toLog(double num) {

                    return Math.log10(num);
                }
            });
            datasets[rddindex++]=resultJavaRDD;
        }

        FileWriter fw = null;
        try {
        //如果文件存在，则追加内容；如果文件不存在，则创建文件
//            File f=new File("D:\\result.txt" );
            File f=new File("/hadoop/result.txt" );
            fw = new FileWriter(f, true);//true,进行追加写。
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        ConcurrentHashMap<String,List<Result>> concurrentHashMap = new ConcurrentHashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for(int i=0;i<datasets.length;i++) {
            final int tem = i;
//            if(datasets[i].collect()==null){
//                System.out.println("为空");
//            }
//            else {
//
                List<Result> results = datasets[i].collect();
            System.out.println(results);
                results.forEach(result -> {
                    pw.println(result.getSon() + ": " + result.getParents());
                    System.out.println("=============节点son: " + result.getSon() + ",fa: " + result.getParents());
                });
//
//            }
//            executorService.execute(()->{
//                List<Result> results = datasets[tem].collect();
//                concurrentHashMap.put("RDD_"+tem,results);
//                results.forEach(result -> {
//                    System.out.println("节点son: " + result.getSon() + ",fa: " + result.getParents());
//                });
//            });
        }
//        concurrentHashMap.forEach((k,v)->{
//            int size = v.size();
//            for(int i=0;i<size;i++){
//                pw.println(v.get(i).getSon() + ": " + v.get(i).getParents());
//            }
//        });
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sc.close();
    }
    private static List<List<Integer>> loadDataSet(String filename)
            throws IOException {
        List<List<Integer>> dataSet = new ArrayList<>();
        Reader reader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        int i=0;
        while ((line=bufferedReader.readLine())!=null){
            i++;
            if(i==1){
                continue;
            }
            ArrayList<Integer> data = new ArrayList<>();
            String[] s = line.split(",");
            for(int j=1;j<s.length;j++){
                data.add(Double.valueOf(s[j]).intValue());
            }
            dataSet.add(data);
        }
        return dataSet;
    }
    private static ArrayList<Integer> getOrder(String filename) throws IOException {
        Reader reader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        ArrayList<Integer> data = new ArrayList<>();
        String[] s  =bufferedReader.readLine().split(",");
        for(int j=0;j<s.length;j++){
            data.add(Double.valueOf(s[j]).intValue());
        }
        return data;
    }
}
