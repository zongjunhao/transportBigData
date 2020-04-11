package com.zuel.syzc.spark.crowd;

import com.zuel.syzc.spark.init.Init;
import com.zuel.syzc.spark.kit.GetCells;
import com.zuel.syzc.spark.kit.Point;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Serializable;
import scala.Tuple2;

import java.util.*;

public class crowdDensity {

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        // 调用算法,计算在某个原型区域内的流入流出人数
        String crowdFlow = new crowdDensity().crowdInflowAndOutflow(spark, "2018-10-02-09", "2018-10-03-12", 123.4159698, 41.80778122, 1000);
        System.out.println(crowdFlow);
        // 计算在某个指定多边形区域内的流入流出人数
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(1, 0));
        points.add(new Point(2, 1));
        points.add(new Point(1, 2));
        points.add(new Point(0, 2));
        points.add(new Point(1, 1));
        String crowdInOutFlow = new crowdDensity().crowdInflowAndOutflow(spark, "2018-10-02-09", "2018-10-03-12", points);
        System.out.println(crowdInOutFlow);
    }

    /**
     * 判断某个时间段，在某个自定义多边形内的人口流动情况
     * @param spark spark上下文对象
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param points 自定义多边形的顶点
     * @return 输入人口数和输出人口数
     */
    public String crowdInflowAndOutflow(SparkSession spark, String startTime,String endTime,List<Point> points) {
        // spark core上下文对象
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        // 获取在某个区域内的基站Id，返回的是List(CellId,flag)
        JavaPairRDD<String, Integer> cellListRdd = sc.parallelizePairs(new GetCells().getCellsInPolygon(points));
        String result = calculateInflowAndOutFlow(spark, startTime, endTime, cellListRdd);
        return result;
    }

    /**
     * 判断某个时间段，在某个圆形区域内的人口流动情况
     * @param spark spark上下文对象
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param radius 中心点半径
     * @return 输入人口量和输出人口量
     */
    public String crowdInflowAndOutflow(SparkSession spark, String startTime,String endTime,double longitude, double latitude, double radius) {
        // spark core上下文对象
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        // 获取在某个区域内的基站Id，返回的是List(CellId,flag)
        JavaPairRDD<String, Integer> cellListRdd = sc.parallelizePairs(new GetCells().getCellsInCircle(longitude,latitude,radius));
        String result = calculateInflowAndOutFlow(spark, startTime, endTime, cellListRdd);
        return result;
    }

    /**
     * 进行核心计算
     * @param spark spark上下文对象
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @param cellListRdd 已经标记好的cellList
     * @return inflow&outFlow
     */
    private String calculateInflowAndOutFlow(SparkSession spark, String startTime,String endTime,JavaPairRDD<String, Integer> cellListRdd){
        // spark core上下文对象
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        // 获取清洗后的数据集
        Dataset<Row> cleanedData = new Init().getCleanedData(spark);
        // 将DataSet转化为Rdd
        JavaRDD<Row> cleanedJavaRdd = cleanedData.javaRDD();
        // 使用filter算子筛选出在指定时间内所有人活动的数据
        JavaRDD<Row> filteredRdd = cleanedJavaRdd.filter(row -> {
            long time = Long.parseLong((String) row.get(0));
            if ((DateUtil.getDayHour(startTime)<time)&&(time<DateUtil.getDayHour(endTime))){
                return true;
            } else {
                return false;
            }
        });
        // 使用mapToPair算子将row数据转化为tuple格式（cellId,(userId,time))
        JavaPairRDD<String, Tuple2<String, Long>> mapedRdd = filteredRdd.mapToPair(row -> {
            long time = Long.parseLong((String) row.get(0));
            String userId = (String) row.get(1);
            String cellId = (String) row.get(3);
            return new Tuple2<>(cellId, new Tuple2<>(userId, time));
        });
        // 将过滤后用户数据和基站数据根据key(cellId)合并,返回的是(value1,value2)
        JavaPairRDD<String, Tuple2<Tuple2<String, Long>, Integer>> joinedRdd = mapedRdd.join(cellListRdd);
        // 转换数据格式，将userId作为key,(userId,(time,flag))
        JavaPairRDD<String, Tuple2> userPlaceRdd = joinedRdd.mapToPair(row -> {
            long time = row._2._1._2;
            String userId = row._2._1._1;
            Integer flag = row._2._2;
            return new Tuple2<>(userId, new Tuple2(time, flag));
        });
        // 设置累加器，计算人口流入量和流出量
        Accumulator<Integer> inflow = sc.accumulator(0);
        Accumulator<Integer> outflow = sc.accumulator(0);
        // groupByKey算子将用户数据key(userId)分类，返回(userId,Iterator<values>)
        userPlaceRdd.groupByKey().foreach(row -> {
            Iterator<Tuple2> iterator = row._2.iterator();
            // 将value转化为list，然后根据time排序
            List<Tuple2<Long, Integer>> timeList = IteratorUtils.toList(iterator);
            timeList.sort(new TimeComparator());
//            int in =0,out=0;
            // 遍历每个用户的出行记录
            for(int i=0;i<timeList.size()-1;i++) {
                if ((timeList.get(i+1)._2 - timeList.get(i)._2) > 0) { // 如果后面再这个地区(1)，但之前不在(0)，说明流入
                    inflow.add(1);
//                    in++;
                } else if ((timeList.get(i+1)._2 - timeList.get(i)._2) < 0){ // 如果后面不这个地区(0)，但之前再这个地区(1)，说明流出
                    outflow.add(1);
//                    out++;
                }
            }
//            System.out.println(row._1+"---in:"+in+"---:"+out);
        });
//        System.out.println(inflow.value());
//        System.out.println(outflow.value());
        return "inflow="+inflow.value()+"|outflow="+outflow.value();
    }
}



class TimeComparator implements Comparator<Tuple2<Long, Integer>>, Serializable {

    @Override
    public int compare(Tuple2<Long, Integer> o1, Tuple2<Long, Integer> o2) {
        return o1._1>o2._1?1:-1;
    }
}
