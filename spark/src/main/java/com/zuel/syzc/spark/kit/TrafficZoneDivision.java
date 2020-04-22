package com.zuel.syzc.spark.kit;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

public class TrafficZoneDivision {
    @Test
    public void divisionTrafficZoneByKmeans() {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        // Loads data.
        spark.read().format("csv").option("header", "true").load("../in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
        Dataset<Row> dataset = spark.sql("select * from longitude");
        JavaRDD<Row> data = dataset.toJavaRDD();

        JavaRDD<Vector> parsedData = data.map(row -> {
            double[] values = new double[2];
            values[0] = Double.parseDouble(row.getString(0));
            values[1] = Double.parseDouble(row.getString(1));
            return Vectors.dense(values);
        });

        // // Load and parse data
        // String path = "data/mllib/kmeans_data.txt";
        // JavaRDD<String> data = jsc.textFile(path);
        // JavaRDD<Vector> parsedData = data.map(s -> {
        //     String[] sarray = s.split(" ");
        //     double[] values = new double[sarray.length];
        //     for (int i = 0; i < sarray.length; i++) {
        //         values[i] = Double.parseDouble(sarray[i]);
        //     }
        //     return Vectors.dense(values);
        // });

        parsedData.cache();

        // Cluster the data into two classes using KMeans
        int numClusters = 200;
        int numIterations = 20;
        KMeansModel clusters = KMeans.train(parsedData.rdd(), numClusters, numIterations);

        System.out.println("Cluster centers:");
        for (Vector center : clusters.clusterCenters()) {
            System.out.println(" " + center);
        }
        double cost = clusters.computeCost(parsedData.rdd());
        System.out.println("Cost: " + cost);

        // Evaluate clustering by computing Within Set Sum of Squared Errors
        double WSSSE = clusters.computeCost(parsedData.rdd());
        System.out.println("Within Set Sum of Squared Errors = " + WSSSE);

        // Save and load model
        // clusters.save(jsc.sc(), "target/org/apache/spark/JavaKMeansExample/KMeansModel");
        // KMeansModel sameModel = KMeansModel.load(jsc.sc(), "target/org/apache/spark/JavaKMeansExample/KMeansModel");
        // sameModel.
        // JavaRDD<Object> result = clusters.predict(parsedData.rdd()).toJavaRDD();
        // result.collect().forEach(System.out::println);
        JavaRDD<Row> result = data.map(row -> {
            double[] values = new double[2];
            values[0] = Double.parseDouble(row.getString(0));
            values[1] = Double.parseDouble(row.getString(1));
            Integer predictResult = clusters.predict(Vectors.dense(values));
            return RowFactory.create(row.getString(0), row.getString(1), row.getString(2), predictResult);
        });
        result.collect().forEach(System.out::println);
    }
}
