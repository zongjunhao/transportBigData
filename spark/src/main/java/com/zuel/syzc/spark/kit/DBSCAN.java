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

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DBSCAN {
    private SparkSession spark;
    public DBSCAN(SparkSession spark) {
        this.spark = spark;
    }

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaRDD<Tuple5<String, Integer, Double, Double, Long>> stdbscan = new DBSCAN(spark).stdbscan( null, null);
        System.out.println("size:"+stdbscan.collect().size());
        stdbscan.collect().forEach(System.out::println);
    }

    /**
     * 基于时空聚类
     * 1. 对每个用户，先使用dbscan聚类
     * 2. 基于KNN思想，对聚类点进行调整
     * 3. 将将用户的轨迹基于时间层面重新聚类
     * @param startTime 起始时间,可以为空
     * @param endTime 结束时间，可以为空
     * @return
     */
    public JavaRDD<Tuple5<String, Integer, Double, Double, Long>> stdbscan(Long startTime,Long endTime){
        JavaRDD<Tuple5<String, Long, String, String, String>> filledRdd = new CleanErraticData(spark).getFilledRdd();
        JavaRDD<Tuple5<String, Long, String, String, String>> filteredRdd = filledRdd.filter(x -> {
            if (startTime == null && endTime == null) {
                return true;
            } else if(startTime == null) {
                return x._2() < endTime;
            } else if(endTime == null) {
                return x._2() >= startTime;
            } else {
                return x._2() > startTime && x._2() < endTime;
            }
        });
        JavaRDD<Tuple5<String, Integer, Double, Double, Long>> finalUserClusterRdd = filteredRdd.groupBy(x -> x._1()).flatMap(x -> {
            String userId = x._1;
//            System.out.println(x._1);
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
//                    .filter(i -> clusterMap.get(new Tuple2<>(i._4(), i._5())) != null)
                    .map(i -> {
                        int cluster = -1;
                        if (clusterMap.get(new Tuple2<>(i._4(), i._5())) != null)
                            cluster = clusterMap.get(new Tuple2<>(i._4(), i._5()))._3();
                        return new UserCluster(i._1(), i._3(), i._4(), i._5(), i._2(), cluster);
                    })
                    .sorted(Comparator.comparing(UserCluster::getTimestamp))
                    .collect(Collectors.toList());
            // KNN ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            int k = 4, maxCluster = (k + 2) / 2;
            for (int i = k / 2; i < userClusterList.size() - k / 2; i++) {
//                System.out.println(i+"----------------------------");
                Map<Integer, Integer> clusterCounter = new HashMap<>();
                // 获取k个
                for (int a = i - k / 2; a < i + k / 2 + 1; a++) {
                    if (a != i) {
                        Integer cId = userClusterList.get(a).getCluster();
                        if (clusterCounter.containsKey(cId)) {// 更新值
                            clusterCounter.put(cId, clusterCounter.get(cId) + 1);
                        } else {
                            clusterCounter.put(cId, 1);
                        }
                    }
                }
                // 对所有的聚类点根据value排序
                List<Map.Entry<Integer, Integer>> sortedMap = new ArrayList<>(clusterCounter.entrySet()).stream()
                        .sorted(Comparator.comparing(Map.Entry::getValue))
                        .collect(Collectors.toList());
                // 如果最大的聚类点不是当前这个类，且不是异常类，且大小大于maxCluster
                // 就将这个类的中心点设为最大中心点，经纬度设为聚类中心的经纬度
                if (!userClusterList.get(i).getCluster().equals(sortedMap.get(sortedMap.size() - 1).getKey())
                        && sortedMap.get(sortedMap.size() - 1).getKey() >= 0
                        && sortedMap.get(sortedMap.size() - 1).getValue() > maxCluster) {
                    UserCluster userCluster = userClusterList.get(i);
                    Integer newCluster = sortedMap.get(sortedMap.size() - 1).getKey();
                    userCluster.setCluster(newCluster);
                    Tuple2<Double, Double> newClusterPosition = clusterCenterMap.get(newCluster);
                    userCluster.setLongitude(newClusterPosition._1.toString());
                    userCluster.setLatitude(newClusterPosition._2.toString());
                }
            }

            // 时间层面聚类！！！！！！！！！！！！！！
            // 根据时间段，重新划分聚类
            List<List<UserCluster>> newUserClusterListAll = new ArrayList<>();
            List<UserCluster> temp = new ArrayList<>();
            for (int i = 0; i < userClusterList.size(); i++) {
                if (i == 0) {
                    temp.add(userClusterList.get(i));
                } else {
                    if (userClusterList.get(i).getCluster().equals(userClusterList.get(i - 1).getCluster())) {
                        temp.add(userClusterList.get(i));
                    } else {
                        newUserClusterListAll.add(temp);
                        temp = new ArrayList<>();
                        temp.add(userClusterList.get(i));
                    }
                }
            }
            newUserClusterListAll.add(temp);
            // 重新计算聚类中心
            m.set(0);
            List<Tuple5<String, Integer, Double, Double, Long>> finalCluster = newUserClusterListAll.stream()
                    .filter(i -> i.get(0).getCluster() >= 0)
                    .map(i -> {
                        double longitude = i.stream().mapToDouble(s -> Double.parseDouble(s.getLongitude())).average().getAsDouble();
                        double latitude = i.stream().mapToDouble(s -> Double.parseDouble(s.getLatitude())).average().getAsDouble();
                        return new Tuple5<>(userId, m.getAndIncrement(), longitude, latitude, i.get(0).getTimestamp());
                    }).collect(Collectors.toList());
            return finalCluster.iterator();
        });
        return finalUserClusterRdd;
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
