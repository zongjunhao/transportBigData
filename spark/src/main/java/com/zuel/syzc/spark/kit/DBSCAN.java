package com.zuel.syzc.spark.kit;

import com.zuel.syzc.spark.init.Init;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.apache.spark.sql.execution.columnar.DOUBLE;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DBSCAN {

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());
        // 获取清洗并合并后的数据集即joined_data
        new Init(spark).init();
        // 获取所有用户的唯一id
        Dataset<Row> uIds = spark.sql("select distinct imsi from joined_data");
        // Dataset转换为List
        List<Row> uIdList = uIds.collectAsList();

        for (Row row : uIdList) {
            String uId = (String) row.get(0);
            // 创建表user_“imsi”存放各个用户的数据
            spark.sql("select * from joined_data where imsi = '" + uId + "'").createOrReplaceTempView("user_" + uId);
        }

        // eps停留点判别距离阈值(单位：米)；minPts停留点判别时间阈值(单位：分钟)
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(0.1, 1);
//        spark.sql("select * from user_460000095074424637").show();

//        spark.sql("select * from user_460000095074424637").withColumn("cluster", );
//        System.out.println(spark.sql("select * from user_460000095074424637").col("longitude"));
//        System.out.println(spark.sql("select * from user_460000095074424637").select("longitude", "latitude"));
//        System.out.println(spark.sql("select * from user_460000095074424637").col("latitude"));
//        spark.sql("select * from user_460000095074424637").select("longitude", "latitude").show();
//        System.out.println(spark.sql("select * from user_460000095074424637").select("longitude", "latitude").collectAsList());
        List<Row> pointList = spark.sql("select * from user_460000095074424637").select("longitude", "latitude").collectAsList();
//        System.out.println(pointList.get(0).get(0));
//        System.out.println(pointList.get(0).get(1));
        List<DoublePoint> doublePoints = new ArrayList<>();
        for (Row row : pointList) {
            double longitude = Double.parseDouble(row.get(0).toString());
            double latitude = Double.parseDouble(row.get(1).toString());
            double[] point = {longitude, latitude};
            DoublePoint doublePoint = new DoublePoint(point);
            doublePoints.add(doublePoint);
        }
//        System.out.println(doublePoints);
//        System.out.println(doublePoints.size());
        List<Cluster> clusters = dbscanClusterer.cluster(doublePoints);
//        System.out.println(clusters.size());
        int cluster_size = clusters.size();
//        System.out.println(clusters.get(0).getPoints());
        List<Integer> cluster_flag = new ArrayList<>();
        for (DoublePoint doublePoint : doublePoints) {
            int j = 0;
            boolean flag = false;
            while (j < cluster_size) {
                if (clusters.get(j).getPoints().contains(doublePoint)) {
                    cluster_flag.add(j + 1);
                    flag = true;
                    break;
                }
                j++;
            }
            if (!flag) {
                cluster_flag.add(0);
            }
        }
//        System.out.println(cluster_flag);
//        System.out.println(cluster_flag.size());
        List<Tuple3<String, String, String>> clusterResult = new ArrayList<>();
//        List<DoublePoint> clusterResult = new ArrayList<>();
        for (int i = 0; i < pointList.size(); i++) {
            String longitude = pointList.get(i).get(0).toString();
            String latitude = pointList.get(i).get(1).toString();
            String cluster = cluster_flag.get(i).toString();
//            double[] point = {longitude, latitude, cluster};
//            DoublePoint doublePoint = new DoublePoint(point);
//            clusterResult.add(doublePoint);
            clusterResult.add(new Tuple3<>(longitude, latitude, cluster));
        }

        JavaRDD<Tuple3<String, String, String>> rdd = sparkContext.parallelize(clusterResult);
        JavaRDD<Row> javaRDD = rdd.map((Function<Tuple3<String, String, String>, Row>) tuple3 ->
                RowFactory.create(tuple3._1(), tuple3._2(), tuple3._3()));
        // 定义转化模式
        StructField[] structFields = {
                new StructField("longitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("latitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("cluster", DataTypes.StringType, true, Metadata.empty())
        };
        StructType schema = new StructType(structFields);
        Dataset<Row> dataset = spark.createDataFrame(javaRDD, schema);
        dataset.createOrReplaceTempView("cluster_460000095074424637");
        spark.sql("select user.timestamp, user.imsi, user.lac_id, user.cell_id, user.longitude, " +
                "user.latitude, cluster_result.cluster from (select * from user_460000095074424637) as user inner join " +
                "(select * from cluster_460000095074424637) as cluster_result " +
                "on user.longitude = cluster_result.longitude and user.latitude = cluster_result.latitude")
                .createOrReplaceTempView("user_cluster_460000095074424637");

        spark.sql("select * from user_460000095074424637").show();
        spark.sql("select * from user_cluster_460000095074424637").show();

    }
}
