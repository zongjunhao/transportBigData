package com.zuel.syzc.spring;

import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.model.entity.Task;
import com.zuel.syzc.spring.utils.SparkSubmit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class ApplicationTests {

    @Autowired
    SparkSubmit sparkSubmit;

    @Autowired
    TaskDao taskDao;

    @Test
    void moduleTest(){
//        String [] params= {"1","fff"};
//        SparkEntry sparkEntry = new SparkEntry();
//        sparkEntry.entry(params);
    }

    @Test
    void sparkSubmitTest(){
        String [] params= {"0","666"};
        sparkSubmit.submit(0,params);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void test(){
        // Task task = new Task();
        // task.setTaskType("1");
        // task.setStartTime(new Date());
        // task.setParams("params");
        // task.setStatus("unfinished");
        // taskDao.insert(task);
        // System.out.println(task);
        // System.out.println(task.getTaskid());
        Task task = new Task();
        task.setTaskid(5);
        task.setEndTime(new Date());
        task.setStatus("finished");
        taskDao.updateById(task);
    }
}
