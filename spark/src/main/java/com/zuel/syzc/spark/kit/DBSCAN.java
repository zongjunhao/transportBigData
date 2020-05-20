package com.zuel.syzc.spark.kit;

import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.init.Init;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.List;

public class DBSCAN {

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());

        Dataset<Row> filledData = new CleanErraticData(spark).getFilledData();
//        filledData.show();

        // 获取清洗并合并后的数据集即joined_data
        new Init(spark).init();
        // 获取所有用户的唯一id
        Dataset<Row> uIds = spark.sql("select distinct imsi from joined_data");
        // Dataset转换为List
        List<Row> uIdList = uIds.collectAsList();
        // 所有用户的id
        List<String> idList = new ArrayList<>();
        for (Row row : uIdList) {
            String uId = (String) row.get(0);
            idList.add(uId);
            // 创建表user_“imsi”存放各个用户的数据
            spark.sql("select * from joined_data where imsi = '" + uId + "'").createOrReplaceTempView("user_" + uId);
        }
        // eps停留点判别距离阈值(单位：米)；minPts停留点判别时间阈值(单位：分钟)
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(0.05, 3);
        for (String id : idList) {
            // 提取每个用户的经纬度数据
            List<Row> pointList = spark.sql("select * from user_" + id).select("longitude", "latitude").collectAsList();
            // 构造可被用于聚类的数据结构(clusterable)
            List<DoublePoint> doublePoints = new ArrayList<>();
            for (Row row : pointList) {
                double longitude = Double.parseDouble(row.get(0).toString());
                double latitude = Double.parseDouble(row.get(1).toString());
                double[] point = {longitude, latitude};
                DoublePoint doublePoint = new DoublePoint(point);
                doublePoints.add(doublePoint);
            }
            // 聚类
            List<Cluster> clusters = dbscanClusterer.cluster(doublePoints);
            // 聚好的类的个数
            int cluster_size = clusters.size();
            // 存放每条记录所属的聚类号
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
            // 将聚类的结果由list转为dataset再转为表
            List<Tuple3<String, String, String>> clusterResult = new ArrayList<>();
            for (int i = 0; i < pointList.size(); i++) {
                String longitude = pointList.get(i).get(0).toString();
                String latitude = pointList.get(i).get(1).toString();
                String cluster = cluster_flag.get(i).toString();
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
            dataset.createOrReplaceTempView("cluster_" + id);
            spark.sql("select user.timestamp, user.imsi, user.lac_id, user.cell_id, user.longitude, " +
                    "user.latitude, cluster_result.cluster from (select * from user_" + id + ") as user inner join " +
                    "(select * from cluster_" + id + ") as cluster_result " +
                    "on user.longitude = cluster_result.longitude and user.latitude = cluster_result.latitude")
                    .createOrReplaceTempView("user_cluster_" + id);

            spark.sql("select * from user_" + id).show();
            spark.sql("select * from user_cluster_" + id).show();
            new KNN(spark, spark.sql("select * from user_cluster_" + id)).processWithKnn(3, 4);
        }
    }
}
