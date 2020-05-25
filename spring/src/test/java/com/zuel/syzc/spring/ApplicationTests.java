package com.zuel.syzc.spring;

import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.model.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class ApplicationTests {

    @Autowired
    TaskDao taskDao;

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
