package com.zuel.syzc.spark.init;

import com.zuel.syzc.spark.constant.Constant;
import com.zuel.syzc.spark.crowd.FourTupleTimeComparator;
import com.zuel.syzc.spark.util.DateUtil;
import com.zuel.syzc.spark.util.LocationUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.Tuple4;
import scala.Tuple5;

import java.util.ArrayList;
import java.util.List;

public class CleanErraticData {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
//        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanedRdd = new CleanErraticData(spark).cleanErraticData(null,null);
//        cleanedRdd.collect();
        JavaRDD<Tuple5<String, Long, String, String, String>> tuple5JavaRDD = new CleanErraticData(spark).cleanErraticDataAll(null, null);
        tuple5JavaRDD.collect().forEach(System.out::println);
    }

    private SparkSession spark;

    public CleanErraticData(SparkSession spark){
        this.spark = spark;
    }

    public JavaRDD<Tuple5<String, Long, String, String, String>> cleanErraticDataAll(Long startTime,Long endTime){
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> groupedCleanedData = cleanErraticData(startTime, endTime);
        JavaRDD<Tuple5<String, Long, String, String, String>> flatmapRdd = groupedCleanedData.flatMap(x -> x._2.stream().map(i -> new Tuple5<>(x._1, i._1(), i._2(), i._3(), i._4())).iterator());
        return flatmapRdd;
    }

    /**
     * 清除数据
     * 漂移数据和乒乓数据
     * @return JavaPairRDD(userId, List(time, cellId, longitude, latitude))
     */
    public JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanErraticData(Long startTime,Long endTime){
        Init initData = new Init(spark);
        JavaRDD<Row> cleanedRdd = initData.init().toJavaRDD();
        JavaPairRDD<String, Iterable<Tuple4<Long, String, String, String>>> userTimeRdd = cleanedRdd
                .filter(x->{
            if (startTime == null && endTime == null) {
                return true;
            } else if (startTime == null) {
                return Long.parseLong(x.getString(0))<endTime;
            } else if (endTime == null) {
                return Long.parseLong(x.getString(0))>startTime;
            } else {
                return Long.parseLong(x.getString(0))>startTime && Long.parseLong(x.getString(0))<endTime;
            }
        }).mapToPair(row -> {
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
            int i;
            for (i = 0; i < trackList.size() - 2; i++) {
                if (!trackList.get(i)._2().equals(trackList.get(i + 1)._2()) && trackList.get(i)._2().equals(trackList.get(i + 2)._2())) {
//                    System.out.println(userId+"---"+trackList.get(i)._2()+"("+DateUtil.getDateFormat(trackList.get(i)._1())+")-"+trackList.get(i+1)._2()+"("+DateUtil.getDateFormat(trackList.get(i+1)._1())+")-"+trackList.get(i+2)._2()+"("+DateUtil.getDateFormat(trackList.get(i+2)._1())+")");
                    if (trackList.get(i + 2)._1() - trackList.get(i)._1() < interval) {
                        deleted.add(trackList.get(i + 1));
                    }
                }
            }
            trackList.removeAll(deleted);
            deleted.clear();
            // 清除ABCA类型数据
            for (i = 0; i < trackList.size() - 3; i++) {
                if (!trackList.get(i)._2().equals(trackList.get(i + 1)._2()) && trackList.get(i)._2().equals(trackList.get(i + 3)._2())) {
                    if (trackList.get(i + 3)._1() - trackList.get(i)._1() < interval) {
                        deleted.add(trackList.get(i + 1));
                        deleted.add(trackList.get(i + 2));
                    }
                }
            }
            trackList.removeAll(deleted); // 删除乒乓数据
            deleted.clear(); // 清空需删除的列表
            // 清除漂移数据
            // 如果某个点前后速度大于300km/s，则将其视为异常数据，将其删除
            int speedInterval = 80;
            boolean isClean = true;
            while (isClean) {
                for (i = 1; i < trackList.size() - 1; i++) {
                    double distance1 = LocationUtils.getDistance(Double.parseDouble(trackList.get(i - 1)._4()), Double.parseDouble(trackList.get(i - 1)._3()),
                            Double.parseDouble(trackList.get(i)._4()), Double.parseDouble(trackList.get(i)._3()));
                    double distance2 = LocationUtils.getDistance(Double.parseDouble(trackList.get(i + 1)._4()), Double.parseDouble(trackList.get(i + 1)._3()),
                            Double.parseDouble(trackList.get(i)._4()), Double.parseDouble(trackList.get(i)._3()));
                    double distance3 = LocationUtils.getDistance(Double.parseDouble(trackList.get(i + 1)._4()), Double.parseDouble(trackList.get(i + 1)._3()),
                            Double.parseDouble(trackList.get(i-1)._4()), Double.parseDouble(trackList.get(i-1)._3()));
                    double beforeV = LocationUtils.getVelocity(distance1,trackList.get(i - 1)._1(),trackList.get(i)._1());
                    double afterV = LocationUtils.getVelocity(distance2,trackList.get(i)._1(),trackList.get(i + 1)._1());
                    double deletedV = LocationUtils.getVelocity(distance3,trackList.get(i - 1)._1(),trackList.get(i + 1)._1());

                    if (beforeV > speedInterval && afterV > speedInterval && deletedV < speedInterval) {
                        deleted.add(trackList.get(i));
                    } else if (distance1>2000 && distance2 > 2000 && distance3 < 2000*2) {
                        deleted.add(trackList.get(i));
                    } else if((beforeV > 200) && (deletedV <200|| deletedV<beforeV)) {
                        deleted.add(trackList.get(i));
                    } else if((afterV>200)&& (deletedV <200|| deletedV<beforeV)) {
                        deleted.add(trackList.get(i));
                    }
                }
                trackList.removeAll(deleted); // 删除乒乓数据
                deleted.clear(); // 清空需删除的列表
                isClean = false;
                for (i=0;i<trackList.size()-1;i++) {
                    double distance2 = LocationUtils.getDistance(Double.parseDouble(trackList.get(i + 1)._4()), Double.parseDouble(trackList.get(i + 1)._3()),
                            Double.parseDouble(trackList.get(i)._4()), Double.parseDouble(trackList.get(i)._3()));
                    double afterV = LocationUtils.getVelocity(distance2,trackList.get(i)._1(),trackList.get(i + 1)._1());
                    if (afterV>200) {
                        isClean = true;
                    }
                }
            }
            return new Tuple2<>(userId, trackList);
        });
//        System.out.println();
        return filteredData;
    }

    /**
     * 填充数据
     * @return imsi,timestamp, cellId,  longitude, latitude
     */
    public JavaRDD<Tuple5<String, Long,String, String, String>> getFilledRdd(Long startTime1,Long endTime1){
        JavaPairRDD<String, List<Tuple4<Long, String, String, String>>> cleanedDataRdd = cleanErraticData(startTime1,endTime1);
        JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd = cleanedDataRdd.map(row -> {
            String userId = row._1;
            List<Tuple4<Long, String, String, String>> trackList = row._2;
//            List<Tuple3<Long, String, String>> trackList = IteratorUtils.toList(row._2.iterator());
//            trackList.sort(new ThreeTupleTimeComparator());
            long interval = Constant.INSERT_TIME_INTERVAL;// 时间切片间隔一个小时
            Long startTime = DateUtil.setUniqueData(trackList.get(0)._1());
            Long endTime = startTime + interval;
            List<Tuple5<String, Long,String, String, String>> finalList = new ArrayList<>();
            for (int i = 0; i < trackList.size(); ) {
                if (startTime < trackList.get(i)._1() && endTime > trackList.get(i)._1()) {// 如果这个数据在这个时间点内
                    finalList.add(new Tuple5<>(userId, trackList.get(i)._1(),trackList.get(i)._2(), trackList.get(i)._3(), trackList.get(i)._4())); // 添加这个数据
                    i++;
                } else if (endTime < trackList.get(i)._1()) { // 如果这个时间段里没有这个数据,补充数据
                    startTime += interval;
                    endTime += interval;
                    if ((i<trackList.size()-1)&&trackList.get(i+1)!=null && trackList.get(i+1)._1()>endTime)
                        finalList.add(new Tuple5<>(userId, startTime, trackList.get(i-1)._2(),trackList.get(i - 1)._3(), trackList.get(i - 1)._4()));
                }
            }
            return finalList;
        }).flatMap(List::iterator);
        return filledRdd;
    }


    /**
     * 根据小时填充数据
     * @return 填充后的数据
     * Dataset<Row<imsi,timestamp, cellId,  longitude, latitude>>
     */
    public Dataset<Row> getFilledData(Long startTime,Long endTime){
        JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd = getFilledRdd(startTime,endTime);
        JavaRDD<Row> rowRdd = filledRdd.map(row -> RowFactory.create(row._1(), row._2(), row._3(), row._4(),row._5()));
//        ArrayList<StructField> fields = new ArrayList<>();
        StructField[] structFields = {
                new StructField("imsi",DataTypes.StringType,true,Metadata.empty()),
                new StructField("timestamp",DataTypes.LongType, true, Metadata.empty()),
                new StructField("cell_id",DataTypes.StringType, true, Metadata.empty()),
                new StructField("longitude",DataTypes.StringType, true,Metadata.empty()),
                new StructField("latitude",DataTypes.StringType, true, Metadata.empty())
        };
        StructType schema = new StructType(structFields);
        return spark.createDataFrame(rowRdd, schema);
    }


}
