package com.zuel.syzc.spark.crowd;

import com.zuel.syzc.spark.constant.Constant;
import com.zuel.syzc.spark.init.Init;
import com.zuel.syzc.spark.kit.TrafficZoneDivision;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import scala.Tuple5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 创建OD矩阵
 * 输入数据：
 * 1. 居民在一段时间内，所有出行的起始点集合
 * 2. 交通小区划分结果
 * 计算步骤：
 * 1. 初始化OD矩阵（小区数*小区数）
 * 2. 将出行起始点映射到小区
 * 3. 遍历数据，向矩阵中填值
 */
public class OdMatrix {
    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        int[][] odMatrix = new OdMatrix().odCalculate(spark, "2018-10-01-00", "2018-10-03-00");
        for (int[] row : odMatrix) {
            for (int i : row) {
                System.out.print(i+" ");
            }
            System.out.println();
        }
    }

    /**
     * 计算od矩阵
     * 1. 获取一段时间内的用户数据
     * 2. 将用户轨迹点与划分的轨迹切分路径区分
     * 4. 计算Od矩阵
     * @param spark spark上下文对象
     * @param startTime
     * @param endTime
     * @return
     */
    public int[][] odCalculate(SparkSession spark, String startTime,String endTime){
        TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision();
        JavaPairRDD<String, Integer> mapCommunityRdd = trafficZoneDivision.divisionTrafficZoneByKmeans(spark);
        // spark core上下文对象
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        int communityNumber = Constant.ZOOM_NUM;
        // 获取初始数据
        JavaRDD<Row> cleanedRdd = new Init(spark).init()
                .javaRDD() // 转化为RDD
                .filter(row->{ // 筛选出特定时间段内的数据
                    long time = Long.parseLong((String) row.get(0));
                    if ((DateUtil.getDayHour(startTime)<time)&&(time<DateUtil.getDayHour(endTime))){
                        return true;
                    } else {
                        return false;
                    }
                });
        // 将清洗后的数据转化格式(cellId,(userId,time))，和经过分区后的数据(cellId, communityId)合并
        // 得到的数据格式为(cellId,((userId,time),communityId))
        JavaPairRDD<String, Tuple2<Tuple2<String, Long>, Integer>> joinedRdd = cleanedRdd.mapToPair(row -> {
            long time = Long.parseLong((String) row.get(0));
            String userId = row.getString(1);
            String cellId = row.getString(3);
            return new Tuple2<>(cellId, new Tuple2<>(userId, time));
        }).join(mapCommunityRdd);
        // 将数据再次转化格式为(userId,(time,communityId))，然后根据userId分组
        JavaPairRDD<String, Iterable<Tuple2<Long, Integer>>> userCommunityRdd = joinedRdd.mapToPair(row -> {
            //(userId,(time,communityId))
            return new Tuple2<>(row._2._1()._1(), new Tuple2<>(row._2._1()._2(), row._2._2()));
        }).groupByKey();
        // 将用户的出行数据根据划分为一个一个的出行段(userId,List<startCommunityId,endCommunityId>)
        JavaPairRDD<String, List<Tuple2<Integer, Integer>>> userOdRdd = userCommunityRdd.mapToPair(row -> {
            String userId = row._1;
            List<Tuple2<Long, Integer>> timeCommunityList = IteratorUtils.toList(row._2.iterator());
//            List<Tuple2<Long, Integer>> timeCommunityList = row._2();
            timeCommunityList.sort(new TimeComparator()); // 根据时间排序
            Integer communityTemp = null;
            List<Tuple2<Integer, Integer>> userOdList = new ArrayList<>();
            // 将用户出行根据起始点划分为出行list，list内容为（起始小区id，结束小区id）
            for (Tuple2<Long, Integer> index : timeCommunityList) {
                if (communityTemp == null) { // 如果是第一个点
                    communityTemp = index._2;
                } else {
                    userOdList.add(new Tuple2<>(communityTemp, index._2));
                    communityTemp = index._2;
                }
            }
            return new Tuple2<>(userId, userOdList);
        });
        JavaPairRDD<Tuple2<Integer, Integer>, Integer> OdAllRdd = userOdRdd
                .flatMap(row -> row._2.iterator()) // 先进行扁平化处理
                .filter(row -> { // 删除包含-1和起始点和重点相等的点
                    if ((row._1 >= 0) && (row._2 >= 0) && (row._1 != row._2))
                        return true;
                    else
                        return false;
                })
                .mapToPair(row -> new Tuple2<>(new Tuple2<>(row._1, row._2), 1)); // 转化格式((开始点，结束点),1)，准备做wordCount
        // reduceByKey将相同key的值的数据相加在一起，相当于做了一次wordCount
        JavaPairRDD<Tuple2<Integer, Integer>, Integer> userOdListRdd = OdAllRdd.reduceByKey((x1, x2) -> (x1 + x2));
        JavaPairRDD<Integer, Integer> inflowRdd = userOdListRdd.mapToPair(row -> new Tuple2<>(row._1._1, row._2)).reduceByKey((x1, x2) -> x1 + x2);
        JavaPairRDD<Integer, Integer> outflowRdd = userOdListRdd.mapToPair(row -> new Tuple2<>(row._1._2, row._2)).reduceByKey((x1, x2) -> x1 + x2);
        int [][] odMatrix = new int[communityNumber+1][communityNumber+1];
        int sum = 0;
        for (Tuple2<Tuple2<Integer, Integer>, Integer> index : userOdListRdd.collect()) {
            odMatrix[index._1._1][index._1._2] = index._2;
        }
        for (Tuple2<Integer, Integer> index : inflowRdd.collect()) {
            odMatrix[index._1][communityNumber] = index._2;
        }
        for (Tuple2<Integer, Integer> index : outflowRdd.collect()) {
            sum += index._2;
            odMatrix[communityNumber][index._1] = index._2;
        }
        odMatrix[communityNumber][communityNumber] = sum;
        return odMatrix;
    }
}

