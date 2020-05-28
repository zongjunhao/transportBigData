package com.zuel.syzc.spark.track;

import com.zuel.syzc.spark.entity.ClusterTransport;
import com.zuel.syzc.spark.entity.TrackFeature;
import com.zuel.syzc.spark.entity.TrackStation;
import com.zuel.syzc.spark.entity.UserTrack;
import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.util.LocationUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
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
import scala.*;

import java.lang.Double;
import java.lang.Long;
import java.util.*;

/**
 * 出行方式分析：
 * 步骤：
 * 1. 数据预处理(乒乓数据，漂移数据) √
 * 2. 将驻留点划分为轨迹段
 * 3. 通过百度API获取驻留点之间的路网数据作为驻留点间距离
 * 4. 计算特征数据：平均数据，出行距离，中位速度，95%位速度，低速度率
 * 5. Kmeans聚类，识别出行方式
 */
public class TrackAnalysis {
    private final SparkSession spark;

    public TrackAnalysis(SparkSession spark) {
        this.spark = spark;
    }

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        new TrackAnalysis(spark).trackAnalysis(null,null);
    }

    public void trackAnalysis(Long startTime,Long endTime){
        // (userId, List(time, cellId, longitude, latitude))
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanedRdd = new CleanErraticData(spark).cleanErraticData(startTime,endTime);
        /*
         * 轨迹划分
         * 1. 计算速度
         * 2. 超过30分钟(时间阈值th3)的速度为0的点被定义为停驻点
         * 3. 如果两个相邻点之间速度差异超过10km/s（速度阈值th4）则被定义为换乘点
         * 4. 如果存在速度为0的点且连续时间不超多20分钟(时间阈值th5),换乘前后的平均速度差值超过10km/s(th4)
         * 5. 识别点间的轨迹为travel，每条子轨迹进行唯一编号tID = userId_i
         * 6. 将数据根据轨迹分组，并计算每条轨迹的5个特征值
         */
        JavaRDD<Tuple2<List<UserTrack>, List<Tuple3<TrackFeature, TrackStation, TrackStation>>>> mapRdd = cleanedRdd.filter(x->x._2.size()>2).map(x -> {
            String userId = x._1;
            List<Tuple4<Long, String, String, String>> trackerList = x._2;
//            if (trackerList.size()<2) {
//                return null;
//            }
            List<UserTrack> userTrackDetail = new ArrayList<>();
//            List<Tuple5<Long, String, String, String, String>> velocityList = new ArrayList<>();
            int tracker = 0, travel = 0, start = 0, end, i = 0, travelStart = i;
            List<Tuple2<Double, Double>> trackerV = new ArrayList<>();
            List<Tuple3<TrackFeature, TrackStation, TrackStation>> trackerFeatureList = new ArrayList<>();
            TrackFeature trackerFeature;
            double beforeV, afterV, distance;
            distance = LocationUtils.getDistance(
                    Double.parseDouble(trackerList.get(i)._4()), Double.parseDouble(trackerList.get(i)._3()),
                    Double.parseDouble(trackerList.get(i + 1)._4()), Double.parseDouble(trackerList.get(i + 1)._3()));
            afterV = LocationUtils.getVelocity(distance, trackerList.get(i)._1(), trackerList.get(i + 1)._1());
            if (afterV < 0.0001) {
                start = i;
            }
            trackerV.add(new Tuple2<>(afterV, distance));
            userTrackDetail.add(new UserTrack(trackerList.get(i), userId, userId + "-" + travel + "-" + tracker));
//            velocityList.add(new Tuple5<>(trackerList.get(i)._1(),trackerList.get(i)._2(),trackerList.get(i)._3(),trackerList.get(i)._4(),travel+"-"+tracker));
            for (i = 1; i < trackerList.size() - 1; i++) {
                if (trackerList.get(i)._1().equals(trackerList.get(i + 1)._1())) {
                    continue;
                }
                beforeV = LocationUtils.getVelocity(
                        Double.parseDouble(trackerList.get(i - 1)._4()), Double.parseDouble(trackerList.get(i - 1)._3()),
                        Double.parseDouble(trackerList.get(i)._4()), Double.parseDouble(trackerList.get(i)._3()),
                        trackerList.get(i - 1)._1(), trackerList.get(i)._1());
                distance = LocationUtils.getDistance(
                        Double.parseDouble(trackerList.get(i)._4()), Double.parseDouble(trackerList.get(i)._3()),
                        Double.parseDouble(trackerList.get(i + 1)._4()), Double.parseDouble(trackerList.get(i + 1)._3()));
                afterV = LocationUtils.getVelocity(distance, trackerList.get(i)._1(), trackerList.get(i + 1)._1());
                if (beforeV > 0 && afterV < 0.001) {
                    start = i; // 重置start&end
                } else if (beforeV < 0.001 && afterV > 0) { // 停滞点
                    end = i;
                    if (trackerList.get(end)._1() - trackerList.get(start)._1() > 1000 * 60 * 30) {
                        trackerFeature = CalculateFeature.calculateFeature(trackerV, userId + "-" + travel + "-" + tracker);
                        if (trackerFeature != null)
                            trackerFeatureList.add(new Tuple3<TrackFeature, TrackStation, TrackStation>(trackerFeature,
                                    new TrackStation(trackerList.get(travelStart)._2(), trackerList.get(travelStart)._1()),
                                    new TrackStation(trackerList.get(i)._2(), trackerList.get(i)._1())));
                        travelStart = i;
                        travel++;
                        tracker++;
                        trackerV.clear();
                    } else if (Math.abs(beforeV - afterV) > 10 && trackerList.get(end)._1() - trackerList.get(start)._1() < 1000 * 60 * 10) {
                        trackerFeature = CalculateFeature.calculateFeature(trackerV, userId + "-" + travel + "-" + tracker);
                        if (trackerFeature != null)
                            trackerFeatureList.add(new Tuple3<TrackFeature, TrackStation, TrackStation>(trackerFeature,
                                    new TrackStation(trackerList.get(travelStart)._2(), trackerList.get(travelStart)._1()),
                                    new TrackStation(trackerList.get(i)._2(), trackerList.get(i)._1())));
                        travelStart = i;
                        tracker++;
                        trackerV.clear();
                    }
                } else if (Math.abs(beforeV - afterV) > 20 && beforeV < 80 && afterV < 80) {
                    trackerFeature = CalculateFeature.calculateFeature(trackerV, userId + "-" + travel + "-" + tracker);
                    if (trackerFeature != null)
                        trackerFeatureList.add(new Tuple3<TrackFeature, TrackStation, TrackStation>(trackerFeature,
                                new TrackStation(trackerList.get(travelStart)._2(), trackerList.get(travelStart)._1()),
                                new TrackStation(trackerList.get(i)._2(), trackerList.get(i)._1())));
                    travelStart = i;
                    tracker++;
                    trackerV.clear();
                }

                trackerV.add(new Tuple2<>(afterV, distance));
                userTrackDetail.add(new UserTrack(trackerList.get(i), userId, userId + "-" + travel + "-" + tracker));
            }
            return new Tuple2<>(userTrackDetail, trackerFeatureList);
        });
        JavaRDD<Row> mapRow = mapRdd.flatMap(x -> x._1.iterator()).map(x -> RowFactory.create(x.getUserId(), x.getCellId(), x.getLongitude(), x.getLatitude(), x.getTimestamp(), x.getTrackId()));
        StructType schema = new StructType(new StructField[]{
                new StructField("user_id", DataTypes.StringType, true, Metadata.empty()),
                new StructField("cell_id", DataTypes.StringType, true, Metadata.empty()),
                new StructField("longitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("latitude", DataTypes.StringType, true, Metadata.empty()),
                new StructField("timestamp", DataTypes.LongType, true, Metadata.empty()),
                new StructField("track_id", DataTypes.StringType, true, Metadata.empty()),
        });
        Dataset<Row> trackDf = spark.createDataFrame(mapRow, schema);
        trackDf.show();
        trackDf.write().format("jdbc").mode(SaveMode.Overwrite)
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data?rewriteBatchedStatements=true")
                .option("dbtable", "user_track_detail")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();

        JavaRDD<Tuple3<TrackFeature, TrackStation, TrackStation>> featureRdd = mapRdd.flatMap(x -> x._2.iterator());
//        featureRdd.collect().forEach(System.out::println);
        JavaRDD<Vector> vectorFeatureRdd = featureRdd.map(x -> {
            double[] values = new double[5];
            // avgV,distance,midV,p95V,minV
            TrackFeature trackFeature = x._1();
            values[0] = trackFeature.getAvgV();
            values[1] = trackFeature.getDistance();
            values[2] = trackFeature.getMidV();
            values[3] = trackFeature.getP95v();
            values[4] = trackFeature.getMinV();
            return Vectors.dense(values);
        });
        vectorFeatureRdd.cache();
        int numClusters = 4;
        int numIterations = 200;
        KMeansModel clusters = KMeans.train(vectorFeatureRdd.rdd(), numClusters, numIterations);
//        System.out.println("avgV,distance,midV,p95V,minV");
//        for (Vector center : clusters.clusterCenters()) {
//            System.out.println(center);
//        }

        List<ClusterTransport> list = new ArrayList<>();
        for (int i=0;i<clusters.clusterCenters().length;i++) {
            ClusterTransport clusterTransport = new ClusterTransport();
            clusterTransport.setCluster(i);
            clusterTransport.setAvgV(clusters.clusterCenters()[i].toArray()[0]);
            list.add(clusterTransport);
        }
        list.sort(ClusterTransport::compareTo);
        Map<Integer,String> clusterMap = new HashMap<>();
        clusterMap.put(list.get(0).getCluster(),"walk");
        clusterMap.put(list.get(1).getCluster(),"bus");
        clusterMap.put(list.get(2).getCluster(),"subway");
        clusterMap.put(list.get(3).getCluster(),"highway");
//        System.out.println(clusterMap);
        JavaRDD<Row> rowRdd = featureRdd.map(x -> {
            double[] values = new double[5];
            TrackFeature trackFeature = x._1();
            TrackStation trackStation = x._2();
            TrackStation trackStation1 = x._3();
            values[0] = trackFeature.getAvgV();
            values[1] = trackFeature.getDistance();
            values[2] = trackFeature.getMidV();
            values[3] = trackFeature.getP95v();
            values[4] = trackFeature.getMinV();
            Integer predictResult = clusters.predict(Vectors.dense(values));
//            System.out.println(x._1() + "--" + trackStation + "--" + trackStation1 + "-------" + clusterMap.get(predictResult));
            return RowFactory.create(trackFeature.getTrackId(), trackStation.getCellId(), trackStation.getTimestamp(), trackStation1.getCellId(), trackStation1.getTimestamp(),clusterMap.get(predictResult));
        });
        schema = new StructType(new StructField[]{
                new StructField("track_id", DataTypes.StringType, true, Metadata.empty()),
                new StructField("start_cell", DataTypes.StringType, true, Metadata.empty()),
                new StructField("start_timestamp", DataTypes.LongType, true, Metadata.empty()),
                new StructField("end_cell", DataTypes.StringType, true, Metadata.empty()),
                new StructField("end_timestamp", DataTypes.LongType, true, Metadata.empty()),
                new StructField("track_way", DataTypes.StringType, true, Metadata.empty()),
        });
        trackDf = spark.createDataFrame(rowRdd, schema);
        trackDf.show();
        trackDf.write().format("jdbc").mode(SaveMode.Overwrite)
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data?rewriteBatchedStatements=true")
                .option("dbtable", "track_way")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();
    }

    static class CalculateFeature{
        /**
         * 对每一个tracker计算特征值，并进行存储
         * @param trackerV trackerList
         * @return (avgV,distance,midV,p95V,minV)
         */
        public static TrackFeature calculateFeature(List<Tuple2<Double,Double>> trackerV,String trackerId){
            trackerV.sort((x1,x2)->x1._1.compareTo(x2._1));
            //计算特征数据：平均速度，出行距离，中位速度，95%位速度，低速度率
            double avgV = trackerV.stream().mapToDouble((x)->x._1).average().orElseGet(null);
            double minV = trackerV.stream().filter(x->x._1<30).count()*1.0/trackerV.size();
            double distance = trackerV.stream().mapToDouble(x->x._2).sum()/1000;
            double midV;
            if (trackerV.size()%2==0) {
                midV = (trackerV.get(trackerV.size()/2-1)._1 + trackerV.get(trackerV.size()/2)._1)/2;
            } else {
                midV = trackerV.get(trackerV.size()/2)._1;
            }
            double p95V = trackerV.get((int) ((trackerV.size()-1) * 0.95))._1;
            if (avgV + distance + midV + p95V + midV < 0.001)
                return null;
            else
                return new TrackFeature(trackerId,avgV,distance,midV,p95V,minV);
        }
    }
}
