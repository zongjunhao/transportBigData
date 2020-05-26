package com.zuel.syzc.spring;

import org.apache.spark.SparkConf;
import org.apache.spark.deploy.SparkApplication;
import org.apache.spark.deploy.SparkSubmit;
import org.apache.spark.deploy.rest.*;
import scala.Tuple2;

import java.util.HashMap;
import java.util.Map;


public class Test implements SparkApplication {
    public static void main(String[] args) {
        String [] arg0=new String[]{
                "--master","spark://192.168.1.101:7077",//ip端口
                "--deploy-mode","cluster",
                "--name","test1",
                "--class","com.zuel.syzc.spark.SparkEntry",//运行主类main
                "--executor-memory","2G",
                "--total-executor-cores","10",
                "--executor-cores","2",
                "C:\\Users\\crescent\\Desktop\\大三下\\transportBigData\\spark\\target\\spark-1.0-SNAPSHOT.jar",//在linux上的包 可改为hdfs上面的路径
                "LR", "1", "66"//jar中的参数，注意这里的参数写法
        };
        SparkSubmit.main(arg0);
//        Test test = new Test();
//        String id = test.submit();
//        boolean flag;
//        while (true){
//            flag = test.monitory(id);
//            if (flag) {
//                break;
//            }
//        }
//        System.out.println("spark执行完成");
    }

    public static String submit() {
        String appResource = "hdfs://cluster1/queryLog-1.0-SNAPSHOT.jar";
        String mainClass = "SelectLog";
        String[] args = {

        };  //spark程序需要的参数

        SparkConf sparkConf = new SparkConf();

        sparkConf.setMaster("spark://192.168.1.107:6066");
        sparkConf.set("spark.submit.deployMode", "cluster");
        sparkConf.set("spark.jars", appResource);
        sparkConf.set("spark.driver.supervise", "false");
        sparkConf.setAppName("queryLog"+ System.currentTimeMillis());
        Map<String,String> conf = new HashMap<>();
        conf.put("spark.master","spark://192.168.1.107:6066");
        conf.put("spark.jars", appResource);
        conf.put("spark.submit.deployMode", "cluster");
        conf.put("spark.appname","test-submit");

        CreateSubmissionResponse response = null;

        try {
//            CreateSubmissionRequest createSubmissionRequest = client.constructSubmitRequest(appResource, mainClass, args, (scala.collection.immutable.Map<String, String>) conf, RestSubmissionClient.filterSystemEnvironment((scala.collection.immutable.Map<String, String>) System.getenv()));
//            SubmitRestProtocolResponse submission = client.createSubmission(createSubmissionRequest);
//            return submission.toJson();
//            response = (CreateSubmissionResponse)
//                    RestSubmissionClient.run(appResource, mainClass, args, sparkConf, new HashMap<String,String>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
//        return response.submissionId();

    }


//    private static RestSubmissionClient client = new RestSubmissionClient("spark://192.168.1.107:7077");

    public static boolean monitory(String appId){
        SubmissionStatusResponse response = null;
        boolean finished =false;
        try {
//            response = (SubmissionStatusResponse) client.requestSubmissionStatus(appId, true);
//            if("FINISHED" .equals(response.driverState()) || "ERROR".equals(response.driverState())){
//                finished = true;
//            }
//            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finished;
    }

    public void run(String appResource, String mainClass, String[] args, SparkConf conf,Map<String,String> env) {
//        String orElse = conf.getOption("spark.master").getOrElse(() -> {
//            throw new IllegalArgumentException("spark.master must be set");
//        });
//        Tuple2<String, String>[] all = conf.getAll();
//        scala.collection.immutable.Map map = new scala.collection.immutable.Map() {
//        all};
    }

    @Override
    public void start( String[] args, SparkConf conf) {

    }
}
