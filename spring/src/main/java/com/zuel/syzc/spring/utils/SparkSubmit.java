package com.zuel.syzc.spring.utils;

import com.zuel.syzc.spring.constant.Constant;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.model.entity.Task;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Component
public class SparkSubmit {

    @Autowired
    private TaskDao taskDao;

    public static void main(String[] args) {
        String [] params= {"0","666"};
        new SparkSubmit().launch(0,params);
    }

    public int submit(int taskType, String[] params) {
        String[] jarParam = {String.valueOf(taskType), String.join("#", params)};
        Task task = new Task();
        task.setTaskType(jarParam[0]);
        task.setParams(jarParam[1]);
        task.setStatus("unfinished");
        task.setStartTime(new Date());
        taskDao.insert(task);
        int taskId = task.getTaskid();
        new Thread(() -> launch(taskId, jarParam)).start();
        return taskId;
    }

    public void launch(int taskId, String[] jarParam) {
        // String[] param = new String[]{
        //         "zjh", "zjh"
        // };
        // 待提交给spark集群处理的spark application jar（即appJar）所在路径
        SparkLauncher launcher = new SparkLauncher();
        launcher.setAppResource(Constant.APP_JAR_PATH);
        // 设置spark driver主类，即appJar的主类
        launcher.setMainClass("com.zuel.syzc.spark.SparkEntry");
        launcher.setAppName("name");
        // 添加传递给spark driver main方法的参数
        launcher.addAppArgs(jarParam);
        // 设置该spark application的master
        launcher.setMaster(Constant.MASTER); // 在yarn-cluster上启动，也可以再local[*]上
        // 关闭spark submit的详细报告
        launcher.setVerbose(true);
//        launcher.setDeployMode("cluster");
        // 设置用于执行appJar的spark集群分配的driver、executor内存等参数
        launcher.setConf(SparkLauncher.DRIVER_MEMORY, "2g");
        launcher.setConf(SparkLauncher.EXECUTOR_MEMORY, "1g");
        launcher.setConf(SparkLauncher.EXECUTOR_CORES, String.valueOf(16));
//        launcher.setConf("spark.app.name", "submit");
        launcher.setConf("spark.app.id", String.valueOf(UUID.randomUUID()));
        launcher.setConf("spark.default.parallelism", String.valueOf(128));
        launcher.setConf("spark.executor.instances", String.valueOf(16));
        launcher.setSparkHome(Constant.SPARK_HOME);
//        launcher.setConf("yarn.resourcemanager.hostname","192.168.1.101");
//        launcher.setConf("spark.driver.host","localhost");

//        launcher.setDeployMode("cluster");

        try {
            Process process = launcher.launch();
            InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(process.getInputStream(), "input");
            Thread inputThread = new Thread(inputStreamReaderRunnable, "LogStreamReader input");
            inputThread.start();

            InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(process.getErrorStream(), "error");
            Thread errorThread = new Thread(errorStreamReaderRunnable, "LogStreamReader error");
            errorThread.start();

            System.out.println("Waiting for finish...");
            int exitCode = process.waitFor();
            System.out.println("Finished! Exit code:" + exitCode);
            Task task = new Task();
            task.setTaskid(taskId);
            task.setEndTime(new Date());
            if (exitCode == 0) {
                task.setStatus("finished");
                taskDao.updateById(task);
            } else {
                task.setStatus("error");
                taskDao.updateById(task);
                launch(taskId, jarParam);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            Task task = new Task();
            task.setTaskid(taskId);
            task.setEndTime(new Date());
            task.setStatus("error");
            taskDao.updateById(task);
            launch(taskId, jarParam);
        }
    }
}
