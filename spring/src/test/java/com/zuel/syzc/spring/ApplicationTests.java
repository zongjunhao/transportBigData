package com.zuel.syzc.spring;

import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.dao.UserTrackDetailDao;
import com.zuel.syzc.spring.model.entity.Task;
import com.zuel.syzc.spring.service.CrowdDensityService;
import com.zuel.syzc.spring.service.impl.CrowdDensityServiceImpl;
import com.zuel.syzc.spring.utils.SparkSubmit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ApplicationTests {

    @Autowired
    SparkSubmit sparkSubmit;

    @Autowired
    TaskDao taskDao;
    @Autowired
    private CrowdDensityService crowdDensityService;
    @Autowired
    UserTrackDetailDao userTrackDetailDao;
    @Test
    void user(){
        List<String> allUser = userTrackDetailDao.getAllUser();
        System.out.println(allUser);
    }

    @Test
    void moduleTest(){
        Long time = 1538578800000L;
        int i = crowdDensityService.computeHistoryModel(time, null);
        System.out.println("result:"+i);
    }

    @Test
    void sparkSubmitTest(){
        String [] params= {"0","666"};
        sparkSubmit.submit(0,params);
    }

    @Test
    void contextLoads() {
        Task task = Task.builder().build();
        task.setTaskType("5");
        task.setParams(String.join("#", "666"));
        task.setStatus("unfinished");
        task.setStartTime(new Date());
        taskDao.insert(task);
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
        Task task = Task.builder().build();
        task.setTaskid(5);
        task.setEndTime(new Date());
        task.setStatus("finished");
        taskDao.updateById(task);
    }
}
