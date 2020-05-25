package com.zuel.syzc.spark.test;

import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author zongjunhao
 */
public class JavaToSparkTest {

    // public static void main(String[] args) {
    //     String id = submit();
    //     boolean flag;
    //     while (true){
    //         flag = monitory(id);
    //         if (flag) {
    //             break;
    //         }
    //     }
    //     System.out.println("spark执行完成");
    // }
    //
    //
    // public static String submit() {
    //     String appResource = "spark/target/spark-1.0-SNAPSHOT.jar";
    //     String mainClass = "com.zuel.syzc.spark.SparkEntry";
    //     String[] args = {
    //         "张三", "李四"
    //     };  //spark程序需要的参数
    //
    //     SparkConf sparkConf = new SparkConf();
    //
    //     sparkConf.setMaster("spark://127.0.0.1:4040");
    //     sparkConf.set("spark.submit.deployMode", "cluster");
    //     sparkConf.set("spark.jars", appResource);
    //     sparkConf.set("spark.driver.supervise", "false");
    //     sparkConf.setAppName("queryLog" + System.currentTimeMillis());
    //
    //     CreateSubmissionResponse response = null;
    //
    //     try {
    //         response = (CreateSubmissionResponse)
    //                     RestSubmissionClient.run(appResource, mainClass, args, sparkConf, new HashMap<String, String>());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     assert response != null;
    //     return response.submissionId();
    // }
    //
    // private static final RestSubmissionClient client = new RestSubmissionClient("spark://127.0.0.1:4040");
    //
    // public static boolean monitory(String appId) {
    //     SubmissionStatusResponse response = null;
    //     boolean finished = false;
    //     try {
    //         response = (SubmissionStatusResponse) client.requestSubmissionStatus(appId, true);
    //         if ("FINISHED".equals(response.driverState()) || "ERROR".equals(response.driverState())) {
    //             finished = true;
    //         }
    //         Thread.sleep(5000);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return finished;
    // }

    public static void main(String[] args) throws IOException, InterruptedException {
        // String[] param = new String[]{
        //         "--name", "app_name",
        //         "--master", "local[*]",
        //         "spark/target/spark-1.0-SNAPSHOT.jar",
        //         "张三", "李四啊"
        // };
        // SparkSubmit.main(param);
        launcher();
    }

    public static void launcher() throws IOException, InterruptedException {
        String[] param = new String[]{
                "zjh", "zjh"
        };
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 待提交给spark集群处理的spark application jar（即appJar）所在路径
        String appJarName = "spark/target/spark-1.0-SNAPSHOT.jar";
        SparkLauncher launcher = new SparkLauncher();
        launcher.setAppResource(appJarName);
        // 设置spark driver主类，即appJar的主类
        launcher.setMainClass("com.zuel.syzc.spark.SparkEntry");
        // 添加传递给spark driver mian方法的参数
        launcher.addAppArgs(param);
        // 设置该spark application的master
        launcher.setMaster("local[*]"); // 在yarn-cluster上启动，也可以再local[*]上
        // 关闭sparksubmit的详细报告
        launcher.setVerbose(false);
        // 设置用于执行appJar的spark集群分配的driver、executor内存等参数
        launcher.setConf(SparkLauncher.DRIVER_MEMORY, "2g");
        launcher.setConf(SparkLauncher.EXECUTOR_MEMORY, "1g");
        launcher.setConf(SparkLauncher.EXECUTOR_CORES, String.valueOf(16));
        launcher.setConf("spark.app.id", String.valueOf(UUID.randomUUID()));
        launcher.setConf("spark.default.parallelism", String.valueOf(128));
        launcher.setConf("spark.executor.instances", String.valueOf(16));
        launcher.setSparkHome("D:\\spark-2.4.4-bin-hadoop2.7");

        Process process =launcher.launch();
        InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(process.getInputStream(), "input");
        Thread inputThread = new Thread(inputStreamReaderRunnable, "LogStreamReader input");
        inputThread.start();

        InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(process.getErrorStream(), "error");
        Thread errorThread = new Thread(errorStreamReaderRunnable, "LogStreamReader error");
        errorThread.start();

        System.out.println("Waiting for finish...");
        int exitCode = process.waitFor();
        System.out.println("Finished! Exit code:" + exitCode);

    }

    // // 启动执行该application
        // SparkAppHandle handle = launcher.startApplication(new SparkAppHandle.Listener() {
        //                                                       //这里监听任务状态，当任务结束时（不管是什么原因结束）,isFinal（）方法会返回true,否则返回false
        //                                                       @Override
        //                                                       public void stateChanged(SparkAppHandle sparkAppHandle) {
        //                                                           if (sparkAppHandle.getState().isFinal()) {
        //                                                               countDownLatch.countDown();
        //                                                           }
        //                                                           // System.out.println("appId" + sparkAppHandle.getAppId());
        //                                                           System.out.println("state:" + sparkAppHandle.getState().toString());
        //                                                       }
        //
        //
        //                                                       @Override
        //                                                       public void infoChanged(SparkAppHandle sparkAppHandle) {
        //                                                           System.out.println("Info:" + sparkAppHandle.getState().toString());
        //                                                       }
        //                                                   }
        // );
        // System.out.println("The task is executing, please wait ....");
        // //线程等待任务结束
        // countDownLatch.await();
        // System.out.println("The task is finished!");


        //
        // // application执行失败重试机制
        // // 最大重试次数
        // boolean failedflag = false;
        // int maxRetrytimes = 3;
        // int currentRetrytimes = 0;
        // while (handle.getState() != SparkAppHandle.State.FINISHED) {
        //     currentRetrytimes++;
        //     // 每6s查看application的状态（UNKNOWN、SUBMITTED、RUNNING、FINISHED、FAILED、KILLED、 LOST）
        //     Thread.sleep(6000L);
        //     System.out.println("applicationId is: " + handle.getAppId());
        //     System.out.println("current state: " + handle.getState());
        //     if ((handle.getAppId() == null && handle.getState() == SparkAppHandle.State.FAILED) || currentRetrytimes > maxRetrytimes) {
        //         System.out.println(String.format("tried launching application for %s times but failed, exit.", maxRetrytimes));
        //         failedflag = true;
        //         break;
        //     }
        // }
    // }
}
