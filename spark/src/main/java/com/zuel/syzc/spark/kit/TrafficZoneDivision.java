package com.zuel.syzc.spark.kit;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import scala.Tuple2;

/**
 * @author zongjunhao
 */
@SuppressWarnings("unused")
public class TrafficZoneDivision {

    public JavaPairRDD<String, Integer> divisionTrafficZoneByKmeans() {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Loads data.
        JavaRDD<Row> data = spark.read().format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").toJavaRDD();

        JavaRDD<Vector> parsedData = data.map(row -> {
            double[] values = new double[2];
            values[0] = Double.parseDouble(row.getString(0));
            values[1] = Double.parseDouble(row.getString(1));
            return Vectors.dense(values);
        });
        parsedData.cache();

        // 聚类数目
        int numClusters = 200;
        // 迭代次数
        int numIterations = 20;
        KMeansModel clusters = KMeans.train(parsedData.rdd(), numClusters, numIterations);

        System.out.println("Cluster centers:");
        for (Vector center : clusters.clusterCenters()) {
            System.out.println(" " + center);
        }
        double cost = clusters.computeCost(parsedData.rdd());
        System.out.println("Cost: " + cost);

        // Evaluate clustering by computing Within Set Sum of Squared Errors
        double withinSetSumOfSquaredErrors = clusters.computeCost(parsedData.rdd());
        System.out.println("Within Set Sum of Squared Errors = " + withinSetSumOfSquaredErrors);

        JavaPairRDD<String, Integer> result = data.mapToPair(row -> {
            double[] values = new double[2];
            values[0] = Double.parseDouble(row.getString(0));
            values[1] = Double.parseDouble(row.getString(1));
            Integer predictResult = clusters.predict(Vectors.dense(values));
            return new Tuple2<>(row.getString(2), predictResult);
        });
        result.collect().forEach(System.out::println);
        return result;

        /*
        JavaRDD<Row> result = data.map(row -> {
            double[] values = new double[2];
            values[0] = Double.parseDouble(row.getString(0));
            values[1] = Double.parseDouble(row.getString(1));
            Integer predictResult = clusters.predict(Vectors.dense(values));
            return RowFactory.create(row.getString(0), row.getString(1), row.getString(2), predictResult);
        });
        // result.collect().forEach(System.out::println);

        // RDD转换为文本文件，形式为[123.4159698,41.80778122,16789-67567924,152]，无表头
        // result.saveAsTextFile("target/org/apache/spark/result.txt");

        // 定义转化模式
        StructField[] structFields = {
                new StructField("longitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("latitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("laci", DataTypes.StringType, true, Metadata.empty()),
                new StructField("zone", DataTypes.IntegerType, true, Metadata.empty())
        };
        StructType schema = new StructType(structFields);

        Dataset<Row> resultDataset = spark.createDataFrame(result, schema);
        resultDataset.show();
        resultDataset.write().option("header", "true").csv("target/org/apache/spark/station_zone");

         */

    }

    public static void main(String[] args) {
        TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision();
        JavaPairRDD<String, Integer> result = trafficZoneDivision.divisionTrafficZoneByKmeans();
        result.collect().forEach(System.out::println);
    }
}
