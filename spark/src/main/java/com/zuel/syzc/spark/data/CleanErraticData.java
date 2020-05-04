package com.zuel.syzc.spark.data;

import com.zuel.syzc.spark.constant.Constant;
import com.zuel.syzc.spark.crowd.FourTupleTimeComparator;
import com.zuel.syzc.spark.init.Init;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.Tuple4;

import java.util.ArrayList;
import java.util.List;

public class CleanErraticData {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<Row> filledData = new CleanErraticData(spark).getFilledData();
        filledData.show();
//        filledData.collect().forEach(x->System.out.println(x._1()+"--"+DateUtil.getDateFormat(x._2())+"--"+x._3()+"-"+x._4()));
//        new cleanErraticData(spark).cleanErraticData();
    }

    private SparkSession spark;

    public CleanErraticData(SparkSession spark){
        this.spark = spark;
    }

    public JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanErraticData(){
        Init initData = new Init(spark);
        JavaRDD<Row> cleanedRdd = initData.init().toJavaRDD();
        JavaPairRDD<String, Iterable<Tuple4<Long, String, String, String>>> userTimeRdd = cleanedRdd.mapToPair(row -> {
            //timestamp,imsi,lac_id,cell_id,longitude,latitude
            long time = Long.parseLong((String) row.get(0));
            String userId = row.getString(1);
            String longitude = row.getString(4);
            String latitude = row.getString(5);
            String cellId = row.getString(3);
            return new Tuple2<>(userId, new Tuple4<>(time, cellId,longitude, latitude));
        }).groupByKey();
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> filteredData = userTimeRdd.mapToPair(row -> {
            String userId = row._1;
            List<Tuple4<Long, String, String, String>> trackList = IteratorUtils.toList(row._2.iterator());
            trackList.sort(new FourTupleTimeComparator());
            // 清除乒乓数据
            long interval = 15 * 60 * 1000;// 时间阈值15分钟
            // 清除ABA类型数据
            ArrayList<Integer> filter = new ArrayList<>();
            ArrayList<Tuple4<Long, String, String, String>> deleted = new ArrayList<>();
            for (int i = 0; i < trackList.size() - 2; i++) {
                if (!trackList.get(i)._2().equals(trackList.get(i + 1)._2()) && trackList.get(i)._2().equals(trackList.get(i + 2)._2())) {
//                    System.out.println(userId+"---"+trackList.get(i)._2()+"("+DateUtil.getDateFormat(trackList.get(i)._1())+")-"+trackList.get(i+1)._2()+"("+DateUtil.getDateFormat(trackList.get(i+1)._1())+")-"+trackList.get(i+2)._2()+"("+DateUtil.getDateFormat(trackList.get(i+2)._1())+")");
                    if (trackList.get(i + 2)._1() - trackList.get(i)._1() < interval) {
                        deleted.add(trackList.get(i + 1));
                    }
                }
            }
//            System.out.print(userId+"--"+trackList.size()+"-");
            trackList.removeAll(deleted);
            deleted.clear();
            // 清除ABCA类型数据
            for (int i = 0; i < trackList.size() - 3; i++) {
                if (!trackList.get(i)._2().equals(trackList.get(i + 1)._2()) && trackList.get(i)._2().equals(trackList.get(i + 3)._2())) {
//                    System.out.println(userId+"---"+trackList.get(i)._2()+"("+DateUtil.getDateFormat(trackList.get(i)._1())+")-"+
//                            trackList.get(i+1)._2()+"("+DateUtil.getDateFormat(trackList.get(i+1)._1())+")-"+
//                            trackList.get(i+2)._2()+"("+DateUtil.getDateFormat(trackList.get(i+2)._1())+")-"+
//                            trackList.get(i+3)._2()+"("+DateUtil.getDateFormat(trackList.get(i+3)._1())+")");
                    if (trackList.get(i + 3)._1() - trackList.get(i)._1() < interval) {
                        deleted.add(trackList.get(i + 1));
                        deleted.add(trackList.get(i + 2));
                    }
                }
            }
            trackList.removeAll(deleted); // 删除乒乓数据
            deleted.clear(); // 清空需删除的列表
//            System.out.print(trackList.size()+"-");
            // 清除漂移数据
            // 如果某个点前后速度大于300km/s，则将其视为异常数据，将其删除
            int speedInterval = 150;
            for (int i = 1; i < trackList.size() - 1; i++) {
                long beforeMinus = trackList.get(i)._1() - trackList.get(i - 1)._1();
                long afterMinus = trackList.get(i + 1)._1() - trackList.get(i)._1();
                double beforeDistance = LocationUtils.getDistance(
                        Double.parseDouble(trackList.get(i - 1)._4()), Double.parseDouble(trackList.get(i - 1)._3()),
                        Double.parseDouble(trackList.get(i)._4()), Double.parseDouble(trackList.get(i)._3()));
                double afterDistance = LocationUtils.getDistance(
                        Double.parseDouble(trackList.get(i + 1)._4()), Double.parseDouble(trackList.get(i + 1)._3()),
                        Double.parseDouble(trackList.get(i)._4()), Double.parseDouble(trackList.get(i)._3()));
                double beforeV = beforeDistance * 1.0 / beforeMinus * 60 * 60;
                double afterV = afterDistance * 1.0 / afterMinus * 60 * 60;
                if (beforeV > speedInterval && afterV > speedInterval) {
//                    System.out.println(userId+":"+beforeDistance+"-"+beforeMinus*1.0/1000/60+"----"+afterDistance+"-"+afterMinus*1.0/1000/60);
//                    System.out.println(userId+":"+beforeV+"-"+afterV);
                    deleted.add(trackList.get(i));
                }
            }
            trackList.removeAll(deleted); // 删除乒乓数据
            deleted.clear(); // 清空需删除的列表
            return new Tuple2<>(userId, trackList);
        });
        return filteredData;
    }

    /**
     * 根据小时填充数据
     * @return 填充后的数据
     * JavaRDD<Tuple4<imsi, timestamp, longitude, latitude>>
     */
    public Dataset<Row> getFilledData(){
//        Init initData = new Init(spark);
//        JavaRDD<Row> cleanedRdd = initData.getCleanedData().toJavaRDD();
//        JavaPairRDD<String, Iterable<Tuple3<Long, String, String>>> userTimeRdd = cleanedRdd.mapToPair(row -> {//timestamp,imsi,lac_id,cell_id
//            long time = Long.parseLong((String) row.get(0));
//            String userId = row.getString(1);
//            String lacId = row.getString(2);
//            String cellId = row.getString(3);
//            return new Tuple2<>(userId, new Tuple3<>(time, lacId, cellId));
//        }).groupByKey();
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanedDataRdd = cleanErraticData();
        JavaRDD<Tuple4<String, Long, String, String>> filledRdd = cleanedDataRdd.map(row -> {
            String userId = row._1;
            List<Tuple4<Long, String, String, String>> trackList = row._2;
//            List<Tuple3<Long, String, String>> trackList = IteratorUtils.toList(row._2.iterator());
//            trackList.sort(new ThreeTupleTimeComparator());
            long interval = Constant.INSERT_TIME_INTERVAL;// 时间切片间隔一个小时
            Long startTime = DateUtil.setUniqueData(trackList.get(0)._1());
            Long endTime = startTime + interval;
            List<Tuple4<String, Long, String, String>> finalList = new ArrayList<>();
            for (int i = 0; i < trackList.size(); ) {
                if (startTime < trackList.get(i)._1() && endTime > trackList.get(i)._1()) {// 如果这个数据在这个时间点内
                    finalList.add(new Tuple4<>(userId, trackList.get(i)._1(), trackList.get(i)._3(), trackList.get(i)._4())); // 添加这个数据
                    i++;
                } else if (endTime < trackList.get(i)._1()) { // 如果这个时间段里没有这个数据,补充数据
                    startTime += interval;
                    endTime += interval;
                    if ((i<trackList.size()-1)&&trackList.get(i+1)!=null && trackList.get(i+1)._1()>endTime)
                        finalList.add(new Tuple4<>(userId, startTime, trackList.get(i - 1)._3(), trackList.get(i - 1)._4()));
                }
            }
            return finalList;
        }).flatMap(List::iterator);
        JavaRDD<Row> rowRdd = filledRdd.map(row -> RowFactory.create(row._1(), row._2(), row._3(), row._4()));
        ArrayList<StructField> fields = new ArrayList<>();
        StructField field = null;
        field = DataTypes.createStructField("imsi",DataTypes.StringType,true);
        fields.add(field);
        field = DataTypes.createStructField("timestamp",DataTypes.LongType,true);
        fields.add(field);
        field = DataTypes.createStructField("longitude",DataTypes.StringType,true);
        fields.add(field);
        field = DataTypes.createStructField("latitude",DataTypes.StringType,true);
        fields.add(field);
        StructType schema = DataTypes.createStructType(fields);
        return spark.createDataFrame(rowRdd, schema);
    }


}