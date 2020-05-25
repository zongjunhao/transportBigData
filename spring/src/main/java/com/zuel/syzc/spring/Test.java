package com.zuel.syzc.spring;

import com.zuel.syzc.spring.model.entity.Task;

import java.util.Date;

public class Test {

    public void test() {
        Task task = new Task();
        task.setTaskType("1");
        task.setStartTime(new Date());
        task.setParams("params");
    }
}
