package com.zuel.syzc.spark;

import com.zuel.syzc.spark.crowd.CrowdDensity;
import com.zuel.syzc.spark.crowd.OdMatrix;
import com.zuel.syzc.spark.crowd.UnusualCrowdDensity;
import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.kit.TrafficZoneDivision;
import com.zuel.syzc.spark.test.DoTest;
import com.zuel.syzc.spark.track.TrackAnalysis;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple5;

import java.util.Arrays;

/**
 * @author zongjunhao
 */
public class SparkEntry {
    // private final static Logger logger = LoggerFactory.getLogger(SparkEntry.class);
    public int entry(String[] args){
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
//        sparkConf
//                .set("spark.executor.instance","2")
//                .setJars(new String[]{"spark/target/spark-1.0-SNAPSHOT.jar"})
//                // 设置executor的内存大小
//                .set("spark.executor.memory", "512m")
////                // 设置提交任务的yarn队列
////                .set("spark.yarn.queue","spark")
////                // 设置driver的ip地址
//                .set("spark.driver.host","192.168.3.73");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        String taskType = "";
        String[] paramsArray = null;
        System.out.println(Arrays.toString(args));
        if (args.length == 0) {
            return -3;
        } else if (args.length == 1) {
            taskType = args[0];
        }else if (args[1]!=null) {
            taskType = args[0];
            String params = args[1];
            paramsArray = params.split("#");
//            return -2;
        }
        Long startTime,endTime;
        try {
            switch (taskType) {
                case "1":   // 小区划分
                    startTime = paramsArray[0].equals("null")?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision(spark);
                    trafficZoneDivision.divisionTrafficZoneByKmeans(startTime,endTime);
                    break;
                case "2":   // 出行OD矩阵
                    OdMatrix odMatrix = new OdMatrix(spark);
                    assert paramsArray != null;
                    if (paramsArray.length < 1) return -2;
                    startTime = paramsArray[0].equals("null") ?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray.length == 1 || paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    odMatrix.save(startTime, endTime);
                    break;
                case "3":   // 指定区域内的人口流入流出量
                    CrowdDensity crowdDensity = new CrowdDensity(spark);
                    // 起始时间，结束时间，中心点经度，中心点纬度，半径
                    if (paramsArray.length < 5) return -2;
                    startTime = paramsArray[0].equals("null")?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    crowdDensity.crowdInflowAndOutflow(startTime, endTime, Double.parseDouble(paramsArray[2]),
                            Double.parseDouble(paramsArray[3]), Double.parseDouble(paramsArray[4]));
                    break;
                case "4":   // 出行方式分析
                    if (paramsArray.length < 2) return -2;
                    startTime = paramsArray[0].equals("null")?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    TrackAnalysis trackAnalysis = new TrackAnalysis(spark);
                    trackAnalysis.trackAnalysis(startTime,endTime);
                    break;
                case "5":   // 构建历史人群模型
                    UnusualCrowdDensity historyUnusualCrowdDensity = new UnusualCrowdDensity(spark);
                    if (paramsArray.length < 2) return -2;
                    startTime = paramsArray[0].equals("null")?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    historyUnusualCrowdDensity.computeHistoryPeople(startTime,endTime);
                    break;
                case "6":   // 获取特定时间人群分布情况和异常聚集情况
                    UnusualCrowdDensity unusualCrowdDensity = new UnusualCrowdDensity(spark);
                    assert paramsArray != null;
                    if (paramsArray.length < 1) return -2;
                    startTime = paramsArray[0].equals("null") ?null:Long.parseLong(paramsArray[0]);
                    endTime = paramsArray.length == 1 || paramsArray[1].equals("null")?null:Long.parseLong(paramsArray[1]);
                    unusualCrowdDensity.judgeAbnormalCell(startTime,endTime);
                    break;
                default:
                    break;
            }
            return 0;

        }catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) {
        String[] param = {null,null};
        int entry = new SparkEntry().entry(new String[]{"5", String.join("#", param)});
        System.out.println(entry);

    }

//    @Test
    public void test() {
        String[] paramsArray = {"aaa", null, "ccc"};
        System.out.println("paramsArray = " + Arrays.toString(paramsArray));
        String paramsString = String.join("#", paramsArray);
        System.out.println("paramsString = " + paramsString);
        System.out.println("paramsArray[1] = " + paramsArray[1]);
        String[] splitString = paramsString.split("#");
        System.out.println("splitString = " + Arrays.toString(splitString));
    }
}
