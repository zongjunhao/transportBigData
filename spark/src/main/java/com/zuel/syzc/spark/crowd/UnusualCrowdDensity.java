package com.zuel.syzc.spark.crowd;

import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;
import scala.Tuple5;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 计算异常群体聚集
 * 1. 计算每个小区的历史人群
 * -> 每隔一个小时计算每个基站的当前聚集人数
 * -> 按照时间分区计算每个时间段的小区聚集人数平均数
 * -> 计算在n天内每个小区的聚集人数的平均数作为历史人群分布模型
 * 2. 在给定时间内，将区域人数与历史人群分布模型人数进行对比
 * -> 将给定时间匹配时间段
 * -> 计算这个时间段内的小区聚集人数Sx
 * -> 提取历史人群分布模型中的小区聚集人数S
 * -> 动态计算阈值T，若Sx-S>T则视为异常聚集，T的计算分两种情况，若有异常聚集的情况，则通过公式计算，若没有异常聚集的情况，则T = 1.5S
 */
public class UnusualCrowdDensity {
    private final SparkSession spark;

    public UnusualCrowdDensity(SparkSession spark) {
        this.spark = spark;
    }

    private StructType schema = new StructType(new StructField[]{
            new StructField("day", DataTypes.StringType, true, Metadata.empty()),
            new StructField("day_time", DataTypes.StringType, true, Metadata.empty()),
            new StructField("cell_id", DataTypes.StringType, true, Metadata.empty()),
            new StructField("day_hour", DataTypes.LongType, true, Metadata.empty()),
            new StructField("count", DataTypes.DoubleType, true, Metadata.empty()),
            new StructField("is_abnormal", DataTypes.IntegerType, true, Metadata.empty())
    });

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        UnusualCrowdDensity unusualCrowdDensity = new UnusualCrowdDensity(spark);
//        unusualCrowdDensity.computeHistoryPeople(null,null);

//        unusualCrowdDensity.computeHistoryPeople(spark,DateUtil.getTimestamp("2018-10-01 00:00:00.000"),DateUtil.getTimestamp("2018-10-03 23:00:00.000"),filledRdd);
        JavaRDD<Row> rowJavaRDD = unusualCrowdDensity.judgeAbnormalCell(DateUtil.getTimestamp("2018-10-03 12:00:00.000"),null);
//        rowJavaRDD.collect().forEach(System.out::println);
//        unusualCrowdDensity.computeT(spark,DateUtil.getTimestamp("2018-10-03 20:00:00.000"));
    }



    /**
     * 计算cell内历史人数
     * @param startDay 起始日期
     * @param endDay 结束日期
     * filledRdd  (userId,timestamp, cellId,  longitude, latitude)
     */
    public void computeHistoryPeople(Long startDay,Long endDay){
        JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd = new CleanErraticData(spark).getFilledRdd(startDay,endDay);
        /**
         * 输入 filledRdd (userId,timestamp, cellId,  longitude, latitude)
         * 1. filter 筛选出指定时间内的数据
         * 2. groupBy 根据（day天数,day_time某天的时间段,timestamp哪个小时,cellId,userId）作为Key，将数据分组，避免同一时间段用户有多条数据的情况
         * 3. mapToPair ((某天，某时间段，某小时，某用户),1)
         * 4. reduceByKey 相同key的value值相加，每天每个小时每个cell内的人数
         * 输出 ((某天，某时间段，某小时，某用户),小区内人数)
         */
        JavaPairRDD<Tuple4<String, String, Long, String>, Integer> groupRdd = filledRdd
                .groupBy(x -> new Tuple5<>(DateUtil.getDay(x._2()), ComputeKey.computeKey(x._2()), DateUtil.setUniqueData(x._2()), x._3(), x._1()))
                .mapToPair(x -> new Tuple2<>(new Tuple4<>(x._1._1(), x._1._2(), x._1._3(), x._1._4()), 1))
                .reduceByKey(Integer::sum);
        /**
         * 输入 ((某天，某时间段，某小时，某用户),小区内人数)
         * 1. mapToPair ((某天，某时间段，某用户）,(小区内人数，1))
         * 2. reduceByKey 将相同key内的值聚合，获得((某天，某时间段，某小区）,(小区内总人数，value个数))
         * 3. map (某天，某时间段，某小区,小区总人数/value个数=小区人数平均值,是否异常0))
         * 输出 （day,day_time,cellId,count,isAbnormal）
         */
        JavaRDD<Row> cellGroupRow = groupRdd.mapToPair(x -> new Tuple2<>(new Tuple3<>(x._1._1(), x._1._2(), x._1._4()), new Tuple2<>(x._2, 1)))
                .reduceByKey((x1, x2) -> new Tuple2<>(x1._1 + x2._2(), x1._2 + x2._2))
                .map(x -> RowFactory.create(x._1._1(), x._1._2(), x._1._3(), null, x._2._1 *1.0/ x._2._2,0));
        // 将数据转化成DataFrame，存储在mysql中
        Dataset<Row> cellCountDf = spark.createDataFrame(cellGroupRow, schema);
        //存储计算的cell平均人数
        SaveMode saveMode;
        // 如果没有起始时间说明是计算所有的，把所有数据覆盖
        // 如果有起始时间说明是指定时间计算，将新计算的数据添加进去
        if (startDay != null) {
            saveMode = SaveMode.Append;
        } else {
            saveMode = SaveMode.Overwrite;
        }
        cellCountDf.write().format("jdbc").mode(saveMode)
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data")
                .option("dbtable", "cell_count")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();
    }

    /**
     * 获取一定时间内的用户数据
     * 计算在这个时间段内每个cell聚集的人数
     *  filledRdd: userId,time, cellId,longitude, latitude
     * @param startTimestamp 开始时间
     * @param filledRdd 待筛选的数据 (imsi,timestamp, cellId,  longitude, latitude)
     * @return (cellId,count)
     */
    private JavaPairRDD<String, Integer> cellCountRdd(long startTimestamp,JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd){
        JavaRDD<Tuple5<String, Long,String, String, String>> filteredRdd = filledRdd.filter(row -> {
            long endTimestamp = startTimestamp + 60 * 60 * 1000;
            return row._2() > startTimestamp && row._2()< endTimestamp;
        });
        JavaPairRDD<String, Integer> groupSumRdd = filteredRdd
                .groupBy(x -> new Tuple2<>(x._3(), x._1())) // 先根据cellId和userId分组，去除重复的user数据
                .mapToPair(x -> new Tuple2<>(x._1._1, 1)).reduceByKey(Integer::sum); // 计算每个cell中的user个数
        return groupSumRdd;
    }

    /**
     * 计算阈值
     * @param startDay 计算阈值的起始时间
     * @return JavaPairRDD<cellId, threshold>
     */
    public JavaPairRDD<String, Double> computeT(long startDay){
        String dayTime = ComputeKey.computeKey(startDay);
        Dataset<Row> cellCountAllDf = spark.read().format("jdbc")
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data")
                .option("dbtable", "cell_count")
                .option("user", "root").option("password", "root")
                .load();
        cellCountAllDf.createOrReplaceTempView("cell_count");
        JavaRDD<Row> initRdd = spark.sql("select * from cell_count where day_time = '" + dayTime + "'").toJavaRDD();
        JavaPairRDD<String, Double> thresholdRdd = initRdd.mapToPair(x -> new Tuple2<>(x.getString(2), new Tuple3<>(x.getString(0), x.getDouble(4), x.getInt(5))))
                .groupByKey()
                .mapToPair(x -> {
                    String cellId = x._1;
                    List<Tuple3<String, Double, Integer>> cellList = IteratorUtils.toList(x._2.iterator());
                    List<Tuple3<String, Double, Integer>> abnormalList = cellList.stream().filter(i -> i._3() > 0).collect(Collectors.toList());
                    double threshold;
                    if (abnormalList.size() > 0) {
                        abnormalList.sort(Comparator.comparingDouble(Tuple3::_2));
                        double sumWD = 0;
                        double sumW = 0;
                        double weight;
                        // threshold的 计算公式！X = sum(Wi*Di)/sum(Wi)  Wi是权值，Di是不正常人数的数值
                        for (int i = 0; i < abnormalList.size(); i++) {
                            weight = 1 / Math.pow(2, i);
                            sumWD += abnormalList.get(i)._2() * weight;
                            sumW += weight;
                        }
                        threshold = sumWD / sumW;
                    } else {
                        threshold = cellList.stream().mapToDouble(Tuple3::_2).average().getAsDouble() * 1.5;
                    }
                    return new Tuple2<>(cellId, threshold);
                });
        return thresholdRdd;
    }

    public JavaRDD<Row> judgeAbnormalCell(Long startDay,Long endTimestamp){
        if (endTimestamp == null) {
            endTimestamp = startDay + 60 * 60 * 1000;
        }
        JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd = new CleanErraticData(spark).getFilledRdd(startDay,endTimestamp);
//        long startDay = DateUtil.getTimestamp("2018-10-03 23:00:00.000");
        String dayTime = ComputeKey.computeKey(startDay);
        String day = DateUtil.getDay(startDay);
        // 获取当前时间段内每个分区内的人数
        JavaPairRDD<String, Integer> cellCountNowRdd = cellCountRdd(startDay, filledRdd);
        // 从mysql中获取模型数据
        JavaPairRDD<String, Double> thresholdAllRdd = computeT(startDay);
        // 将最新数据与历史数据连接，判断阈值
        JavaRDD<Row> newCellRow = cellCountNowRdd.leftOuterJoin(thresholdAllRdd).map(x -> {
            Integer countNow = x._2._1;
            int isAbnormal = 0;
            Optional<Double> threshold = x._2._2;
            if (threshold.isPresent()) {
                isAbnormal = countNow >= threshold.get() ? 1 : 0;
            }
            Long dayHour = startDay;
            return RowFactory.create(day, dayTime, x._1, dayHour,countNow * 1.0, isAbnormal);
        });
        JavaRDD<Row> abnormalRdd = newCellRow.filter(x -> x.getInt(5) > 0);
        Dataset<Row> abnormalDfFinal = spark.createDataFrame(newCellRow, schema);
        abnormalDfFinal.show();
        abnormalDfFinal.write().format("jdbc").mode(SaveMode.Overwrite)
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data")
                .option("dbtable", "current_cell_count")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();
        return abnormalRdd;
    }

    static class ComputeKey{
        static String computeKey(long time){
            int week = DateUtil.getWeek(time);
            String group = "";
            if (week > 1 && week < 7) {
                group += "weekend-";
                int hour = DateUtil.getHour(time);
                if (hour < 7) {// 0-7
                    group += "0-7";
                } else if (hour < 10) { // 7-10
                    group += "7-10";
                } else if (hour < 12) { // 10-12
                    group += "10-12";
                } else if (hour < 14) { // 12-14
                    group += "12-14";
                } else if (hour < 16) { // 14-16
                    group += "14-16";
                } else if (hour < 20) { // 16-20
                    group += "16-20";
                } else { // 20-24
                    group += "20-24";
                }
            } else {
                group += "weekday-";
                group += "weekend-";
                int hour = DateUtil.getHour(time);
                if (hour < 8) {// 0-8
                    group += "0-8";
                } else if (hour < 11) { // 8-11
                    group += "8-11";
                } else if (hour < 13) { // 11-13
                    group += "11-13";
                } else if (hour < 17) { // 13-17
                    group += "13-17";
                } else if (hour < 20) { // 17-20
                    group += "17-20";
                } else if (hour < 22) { // 20-22
                    group += "20-22";
                } else { // 22-24
                    group += "22-24";
                }
            }
            return group;
        }
    }
}

