package com.zuel.syzc.spark.track;

import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.util.LocationUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.sql.SparkSession;
import scala.*;

import java.lang.Double;
import java.lang.Long;
import java.util.ArrayList;
import java.util.List;

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
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        new TrackAnalysis().trackAnalysis(spark);
    }

    public void trackAnalysis(SparkSession spark){
        // (userId, List(time, cellId, longitude, latitude))
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanedRdd = new CleanErraticData(spark).cleanErraticData();
        /*
         * 轨迹划分
         * 1. 计算速度
         * 2. 超过30分钟(时间阈值th3)的速度为0的点被定义为停驻点
         * 3. 如果两个相邻点之间速度差异超过10km/s（速度阈值th4）则被定义为换乘点
         * 4. 如果存在速度为0的点且连续时间不超多20分钟(时间阈值th5),换乘前后的平均速度差值超过10km/s(th4)
         * 5. 识别点间的轨迹为travel，每条子轨迹进行唯一编号tID = userId_i
         * 6. 将数据根据轨迹分组，并计算每条轨迹的5个特征值
         */
        JavaRDD<Tuple6<String, Double, Double, Double, Double, Double>> featureRdd = cleanedRdd.flatMap(x -> {
            String userId = x._1;
            List<Tuple4<Long, String, String, String>> trackerList = x._2;
//            List<Tuple5<Long, String, String, String, String>> velocityList = new ArrayList<>();
//            System.out.println(userId);
            int tracker = 0, travel = 0, start = 0, end, i = 0;
            List<Tuple2<Double, Double>> trackerV = new ArrayList<>();
            List<Tuple6<String, Double, Double, Double, Double, Double>> trackerFeatureList = new ArrayList<>();
            Tuple6<String, Double, Double, Double, Double, Double> trackerFeature;
            double beforeV, afterV, distance;
            distance = LocationUtils.getDistance(
                    Double.parseDouble(trackerList.get(i)._4()), Double.parseDouble(trackerList.get(i)._3()),
                    Double.parseDouble(trackerList.get(i + 1)._4()), Double.parseDouble(trackerList.get(i + 1)._3()));
            afterV = LocationUtils.getVelocity(distance, trackerList.get(i)._1(), trackerList.get(i + 1)._1());
            if (afterV < 0.0001) {
                start = i;
            }
            trackerV.add(new Tuple2<>(afterV, distance));
//            velocityList.add(new Tuple5<>(trackerList.get(i)._1(),trackerList.get(i)._2(),trackerList.get(i)._3(),trackerList.get(i)._4(),travel+"-"+tracker));
            for (i = 1; i < trackerList.size() - 1; i++) {
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
                            trackerFeatureList.add(trackerFeature);
                        travel++;
                        tracker++;
                        trackerV.clear();
                    } else if (Math.abs(beforeV - afterV) > 10 && trackerList.get(end)._1() - trackerList.get(start)._1() < 1000 * 60 * 10) {
                        trackerFeature = CalculateFeature.calculateFeature(trackerV, userId + "-" + travel + "-" + tracker);
                        if (trackerFeature != null)
                            trackerFeatureList.add(trackerFeature);
                        tracker++;
                        trackerV.clear();
                    }
                } else if (Math.abs(beforeV - afterV) > 20 && beforeV < 80 && afterV < 80) {
                    trackerFeature = CalculateFeature.calculateFeature(trackerV, userId + "-" + travel + "-" + tracker);
                    if (trackerFeature != null)
                        trackerFeatureList.add(trackerFeature);
                    tracker++;
                    trackerV.clear();
                }
                trackerV.add(new Tuple2<>(afterV, distance));
//                velocityList.add(new Tuple5<>(trackerList.get(i)._1(),trackerList.get(i)._2(),trackerList.get(i)._3(),trackerList.get(i)._4(),travel+"-"+tracker));
            }
//            velocityList.add(new Tuple5<>(trackerList.get(i)._1(),trackerList.get(i)._2(),trackerList.get(i)._3(),trackerList.get(i)._4(),travel+"-"+tracker));
//            for (Tuple6<String, Double, Double, Double, Double, Double> index : trackerFeatureList) {
//                System.out.println(index);
//            }

            return trackerFeatureList.iterator();
        });
        featureRdd.collect().forEach(System.out::println);
        JavaRDD<Vector> vectorFeatureRdd = featureRdd.map(x -> {
            double[] values = new double[5];
            values[0] = x._2();
            values[1] = x._3();
            values[2] = x._4();
            values[3] = x._5();
            values[4] = x._6();
            return Vectors.dense(values);
        });
        vectorFeatureRdd.cache();
        int numClusters = 4;
        int numIterations = 100;
        KMeansModel clusters = KMeans.train(vectorFeatureRdd.rdd(), numClusters, numIterations);
        System.out.println("center");
        for (Vector center : clusters.clusterCenters()) {
            System.out.println(center);
        }
    }

    static class CalculateFeature{
        /**
         * 对每一个tracker计算特征值，并进行存储
         * @param trackerV trackerList
         * @return (avgV,distance,midV,p95V,minV)
         */
        public static Tuple6<String,Double,Double,Double,Double,Double> calculateFeature(List<Tuple2<Double,Double>> trackerV,String trackerId){
            trackerV.sort((x1,x2)->x1._1.compareTo(x2._1));
            //计算特征数据：平均速度，出行距离，中位速度，95%位速度，低速度率
            double avgV = trackerV.stream().mapToDouble((x)->x._1).average().getAsDouble();
            double minV = trackerV.get(0)._1;
            double distance = trackerV.stream().mapToDouble(x->x._2).sum()/1000;
            double midV;
            if (trackerV.size()%2==0) {
                midV = (trackerV.get(trackerV.size()/2-1)._1 + trackerV.get(trackerV.size()/2)._1)/2;
            } else {
                midV = trackerV.get(trackerV.size()/2)._1;
            }
            double p95V = trackerV.get((int) (trackerV.size() * 0.95))._1;
            if (avgV + distance + midV + p95V + midV < 0.001)
                return null;
            else
                return new Tuple6<>(trackerId,avgV,distance,midV,p95V,minV);
        }
    }


}
