package com.zuel.syzc.spark.kit;

import com.zuel.syzc.spark.constant.Constant;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple5;
import scala.Tuple6;

/**
 * @author zongjunhao
 */
@SuppressWarnings("unused")
public class TrafficZoneDivision {
    private final SparkSession spark;

    public TrafficZoneDivision(SparkSession spark) {
        this.spark = spark;
    }

    public JavaRDD<Tuple6<String, Integer, Double, Double, Long, Integer>> divisionTrafficZoneByKmeans(Long startTime,Long endTime) {
        // Loads stay data.
        JavaRDD<Tuple5<String, Integer, Double, Double, Long>> stdbscan = new DBSCAN(spark).stdbscan( startTime, endTime);
        // Loads data.
        // JavaRDD<Row> data = spark.read().format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").toJavaRDD();

        JavaRDD<Vector> parsedData = stdbscan.map(row -> {
            double[] values = new double[2];
            values[0] = row._3();
            values[1] = row._4();
            return Vectors.dense(values);
        });
        parsedData.cache();

        // 聚类数目
        int numClusters = Constant.ZOOM_NUM;
        // 迭代次数
        int numIterations = 20;
        KMeansModel clusters = KMeans.train(parsedData.rdd(), numClusters, numIterations);

        double cost = clusters.computeCost(parsedData.rdd());
        // Evaluate clustering by computing Within Set Sum of Squared Errors
        double withinSetSumOfSquaredErrors = clusters.computeCost(parsedData.rdd());
//        System.out.println("Within Set Sum of Squared Errors = " + withinSetSumOfSquaredErrors);
        JavaRDD<Tuple6<String, Integer, Double, Double, Long, Integer>> result = stdbscan.map(row->{
            double[] values = new double[2];
            values[0] = row._3();
            values[1] = row._4();
            Integer predictResult = clusters.predict(Vectors.dense(values));
            return new Tuple6<>(row._1(), row._2(), row._3(), row._4(), row._5(), predictResult);
        });
        JavaRDD<Row> zone = result.map(x -> RowFactory.create(x._3(), x._4(), x._6()));
        // 定义转化模式
        StructField[] structFields = {
                new StructField("longitude", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("latitude", DataTypes.DoubleType, true, Metadata.empty()),
//                new StructField("laci", DataTypes.StringType, true, Metadata.empty()),
                new StructField("zone", DataTypes.IntegerType, true, Metadata.empty())
        };
        StructType schema = new StructType(structFields);

        Dataset<Row> resultDataset = spark.createDataFrame(zone, schema);
        resultDataset.show();
//        resultDataset.write().format("jdbc").mode(SaveMode.Overwrite)
//                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data")
//                .option("dbtable", "zone_division")
//                .option("batchsize",10000)
//                .option("isolationLevel","NONE")
//                .option("truncate","false")
//                .option("user", "root").option("password", "root").save();
        return result;


//        JavaRDD<Row> result = data.map(row -> {
//            double[] values = new double[2];
//            values[0] = Double.parseDouble(row.getString(0));
//            values[1] = Double.parseDouble(row.getString(1));
//            Integer predictResult = clusters.predict(Vectors.dense(values));
//            return RowFactory.create(row.getString(0), row.getString(1), row.getString(2), predictResult);
//        });
        // result.collect().forEach(System.out::println);

        // RDD转换为文本文件，形式为[123.4159698,41.80778122,16789-67567924,152]，无表头
        // result.saveAsTextFile("target/org/apache/spark/result.txt");





    }

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision(spark);
        JavaRDD<Tuple6<String, Integer, Double, Double, Long, Integer>> result = trafficZoneDivision.divisionTrafficZoneByKmeans(null,null);
        result.collect().forEach(System.out::println);
    }
}
