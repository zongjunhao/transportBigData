package com.zuel.syzc.spark.kit;

import com.zuel.syzc.spark.entity.UserCluster;
import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.init.Init;
import org.apache.commons.collections.IteratorUtils;
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
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple5;
import shapeless.Tuple;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DBSCAN {

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        new DBSCAN().dbscan(spark,null,null);


    }

    public void dbscan(SparkSession spark,String startTime,String endTime){
        JavaRDD<Tuple5<String, Long, String, String, String>> filledRdd = new CleanErraticData(spark).getFilledRdd();
        filledRdd.groupBy(x->x._1()).foreach(x->{
            System.out.println(x._1);
            List<Tuple5<String, Long, String, String, String>> list = IteratorUtils.toList(x._2.iterator());
            // eps停留点判别距离阈值(单位：米)；minPts停留点判别时间阈值(单位：分钟)
            DBSCANClusterer dbscanClusterer = new DBSCANClusterer(0.05, 3);
            // 取经度和维度作为参数，放入dbscanClusterer，获取聚类点
            List<DoublePoint> doublePoints = list.stream()
                    .map(i -> new DoublePoint(new double[]{Double.parseDouble(i._4()), Double.parseDouble(i._5())}))
                    .collect(Collectors.toList());
            // 调用dbscan算法，对结果进行聚类
            List<Cluster> clusters = dbscanClusterer.cluster(doublePoints);
            AtomicInteger m = new AtomicInteger(0);
            // 遍历聚类结果，添加聚类的编号
            List<List<Tuple3<Double, Double, Integer>>> clusterList = clusters.stream().map(i -> {
                // 格式转化
                List<DoublePoint> points = i.getPoints();
                // m为聚类点的编号
                int clusterInt = m.get();
                // 对同一个类中的点添加聚类的编号
                List<Tuple3<Double, Double, Integer>> collect = points.stream()
                        .map(n -> new Tuple3<>(n.getPoint()[0], n.getPoint()[1], clusterInt)).collect(Collectors.toList());
                // 类号加1
                m.getAndIncrement();
                return collect;
            }).collect(Collectors.toList());
            // 聚类中心点！！！
            Map<Integer, Tuple2<Double, Double>> clusterCenterMap = clusterList.stream().map(i -> {
                double longitude = i.stream().mapToDouble(Tuple3::_1).average().getAsDouble();
                double latitude = i.stream().mapToDouble(Tuple3::_2).average().getAsDouble();
                return new Tuple3<>(longitude, latitude, i.get(0)._3());
            }).collect(Collectors.toMap(Tuple3::_3, i -> new Tuple2<>(i._1(), i._2())));
            // 所有的聚类点和它对应的集群号！！！
            Map<Tuple2<String, String>, Tuple3<Double, Double, Integer>> clusterMap = clusterList.stream().flatMap(Collection::stream)
                    .collect(Collectors.toMap((i) -> new Tuple2<>(i._1().toString(), i._2().toString()), i -> i));
            // 在用户的轨迹中筛选出聚类出的点
            List<UserCluster> userClusterList = list.stream()
                    .filter(i -> clusterMap.get(new Tuple2<>(i._4(), i._5())) != null)
                    .map(i -> new UserCluster(i._1(), i._3(), i._4(), i._5(), i._2(), clusterMap.get(new Tuple2<>(i._4(), i._5()))._3())).collect(Collectors.toList());
            for (int i=2;i<userClusterList.size()-2;i++) {

            }
        });
    }

    public void current(SparkSession spark){


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
            rdd.foreach(x->System.out.println(x));
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

//            spark.sql("select * from user_" + id).show();
//            spark.sql("select * from user_cluster_" + id).show();
            new KNN(spark, spark.sql("select * from user_cluster_" + id)).processWithKnn(3, 4);
        }

    }
}
