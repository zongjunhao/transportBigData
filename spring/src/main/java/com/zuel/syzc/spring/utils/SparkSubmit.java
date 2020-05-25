package com.zuel.syzc.spring.utils;

import com.zuel.syzc.spring.constant.Constant;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.util.UUID;

public class SparkSubmit {
    public static void submit(int taskType, String[] params) throws IOException, InterruptedException {
        String[] jarParam = {String.valueOf(taskType), String.join("#", params)};
        launch(jarParam);
    }

    public static int launch(String[] jarParam) throws IOException, InterruptedException {
        // String[] param = new String[]{
        //         "zjh", "zjh"
        // };
        // 待提交给spark集群处理的spark application jar（即appJar）所在路径
        SparkLauncher launcher = new SparkLauncher();
        launcher.setAppResource(Constant.APP_JAR_PATH);
        // 设置spark driver主类，即appJar的主类
        launcher.setMainClass("com.zuel.syzc.spark.SparkEntry");
        // 添加传递给spark driver main方法的参数
        launcher.addAppArgs(jarParam);
        // 设置该spark application的master
        launcher.setMaster(Constant.MASTER); // 在yarn-cluster上启动，也可以再local[*]上
        // 关闭spark submit的详细报告
        launcher.setVerbose(false);
        // 设置用于执行appJar的spark集群分配的driver、executor内存等参数
        launcher.setConf(SparkLauncher.DRIVER_MEMORY, "2g");
        launcher.setConf(SparkLauncher.EXECUTOR_MEMORY, "1g");
        launcher.setConf(SparkLauncher.EXECUTOR_CORES, String.valueOf(16));
        launcher.setConf("spark.app.id", String.valueOf(UUID.randomUUID()));
        launcher.setConf("spark.default.parallelism", String.valueOf(128));
        launcher.setConf("spark.executor.instances", String.valueOf(16));
        launcher.setSparkHome(Constant.SPARK_HOME);

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
        return exitCode;
    }
}
