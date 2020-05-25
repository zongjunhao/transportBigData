package com.zuel.syzc.spark;

import com.zuel.syzc.spark.crowd.CrowdDensity;
import com.zuel.syzc.spark.crowd.OdMatrix;
import com.zuel.syzc.spark.crowd.UnusualCrowdDensity;
import com.zuel.syzc.spark.init.CleanErraticData;
import com.zuel.syzc.spark.kit.TrafficZoneDivision;
import com.zuel.syzc.spark.track.TrackAnalysis;
import com.zuel.syzc.spark.util.DateUtil;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import scala.Tuple5;

import java.util.Arrays;

/**
 * @author zongjunhao
 */
public class SparkEntry {
    // private final static Logger logger = LoggerFactory.getLogger(SparkEntry.class);

    public static void main(String[] args) {
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        String taskType = args[0];
        String params = args[1];
        String[] paramsArray = params.split("#");
        switch (taskType) {
            case "1":   // 小区划分
                TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision(spark);
                trafficZoneDivision.divisionTrafficZoneByKmeans();
                break;
            case "2":   // 出行OD矩阵
                OdMatrix odMatrix = new OdMatrix(spark);
                odMatrix.save(paramsArray[0], paramsArray[1]);
                break;
            case "3":   // 指定区域内的人口流入流出量
                CrowdDensity crowdDensity = new CrowdDensity(spark);
                crowdDensity.crowdInflowAndOutflow(paramsArray[0], paramsArray[1], Double.parseDouble(paramsArray[2]),
                        Double.parseDouble(paramsArray[2]), Double.parseDouble(paramsArray[2]));
                break;
            case "4":   // 出行方式分析
                TrackAnalysis trackAnalysis = new TrackAnalysis(spark);
                trackAnalysis.trackAnalysis();
                break;
            case "5":   // 构建历史人群模型
                UnusualCrowdDensity historyUnusualCrowdDensity = new UnusualCrowdDensity(spark);
                JavaRDD<Tuple5<String, Long,String, String, String>> historyFilledRdd = new CleanErraticData(spark).getFilledRdd();
                historyUnusualCrowdDensity.computeHistoryPeople(DateUtil.getTimestamp(paramsArray[0]),DateUtil.getTimestamp(paramsArray[1]),historyFilledRdd);
                break;
            case "6":   // 获取特定时间人群分布情况和异常聚集情况
                UnusualCrowdDensity unusualCrowdDensity = new UnusualCrowdDensity(spark);
                JavaRDD<Tuple5<String, Long,String, String, String>> filledRdd = new CleanErraticData(spark).getFilledRdd();
                unusualCrowdDensity.judgeAbnormalCell(DateUtil.getTimestamp(paramsArray[0]), filledRdd);
                break;
            default:
                break;
        }
    }

    @Test
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
