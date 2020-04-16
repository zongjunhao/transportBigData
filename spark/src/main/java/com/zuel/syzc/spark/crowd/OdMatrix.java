package com.zuel.syzc.spark.crowd;

import com.zuel.syzc.spark.init.Init;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple3;

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

    // 初始化数据
    public int[][] odCalculate(SparkSession spark, String startTime,String endTime){
        // spark core上下文对象
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        int communityNumber = 8;
        JavaRDD<Row> cleanedRdd = new Init(spark).init() // 获取初始数据
                .javaRDD() // 转化为RDD
                .filter(row->{ // 筛选出特定时间段内的数据
                    long time = Long.parseLong((String) row.get(0));
                    if ((DateUtil.getDayHour(startTime)<time)&&(time<DateUtil.getDayHour(endTime))){
                        return true;
                    } else {
                        return false;
                    }
                });
        //timestamp,imsi,place.lac_id,place.cell_id,longitude,latitude
        // 将数据转化为(userId,(time,longitude,latitude))的形式
        // 然后将数据根据key(userId)聚合
        JavaPairRDD<String, Iterable<Tuple3<Long, Double, Double>>> groupedUserRdd = cleanedRdd.mapToPair(row -> { // 将数据转化为(userId,(time,longitude,latitude))的形式
            long time = Long.parseLong((String) row.get(0));
            String userId = row.getString(1);
            Double longitude = Double.parseDouble((String) row.get(4));
            Double latitude = Double.parseDouble((String) row.get(5));
            return new Tuple2<>(userId, new Tuple3<>(time, longitude, latitude));
        }).groupByKey();
        // 将数据根据key(userId)聚合，然后将驻留点与小区进行匹配，返回(userId,List<(time,communityId)>)形式
        JavaPairRDD<String, List<Tuple2<Long, Integer>>> userCommunityRdd = groupedUserRdd.mapToPair(row -> {
            String userId = row._1;
            Iterator<Tuple3<Long, Double, Double>> iterator = row._2.iterator();
            List<Tuple2<Long, Integer>> timeCommunityList = new ArrayList<>();
            while (iterator.hasNext()){
                Tuple3<Long, Double, Double> index = iterator.next();
                int k = (int) ((index._2() + index._3() + Math.random() * 10) % (communityNumber+1))-1; // 根据经纬度模拟小区
                timeCommunityList.add(new Tuple2<>(index._1(), k));  // list里的格式为(time,communityId)
//                System.out.println(userId+"---");
            }
            return new Tuple2<>(userId, timeCommunityList);
        });
        // 将用户的出行数据根据划分为一个一个的出行段(userId,List<startCommunityId,endCommunityId>)
        JavaPairRDD<String, List<Tuple2<Integer, Integer>>> userOdRdd = userCommunityRdd.mapToPair(row -> {
            String userId = row._1;
            List<Tuple2<Long, Integer>> timeCommunityList = row._2();
            timeCommunityList.sort(new TimeComparator()); // 根据时间排序
            Integer communityTemp = null;
            List<Tuple2<Integer, Integer>> userOdList = new ArrayList<>();
            // 将用户出行根据起始点划分为出行list，list内容为（起始小区id，结束小区id）
            for (Tuple2<Long, Integer> index : timeCommunityList) {
                if (communityTemp == null) {
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

