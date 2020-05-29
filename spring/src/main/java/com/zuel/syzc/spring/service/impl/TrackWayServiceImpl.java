package com.zuel.syzc.spring.service.impl;

import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.dao.TrackWayDao;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.Task;
import com.zuel.syzc.spring.model.entity.TrackWay;
import com.zuel.syzc.spring.service.TrackWayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TrackWayServiceImpl implements TrackWayService {
    @Autowired
    private TrackWayDao trackWayDao;
    @Autowired
    private TaskDao taskDao;
    private SparkEntry sparkEntry = new SparkEntry();

    @Override
    public List<UserTrack> getTrackWay(String userId, Long startTime, Long endTime) {
//        System.out.println(userId+","+startTime+","+endTime);
        String[] param = {String.valueOf(startTime),String.valueOf(endTime)};
        Task task = Task.builder().startTime(new Date()).taskType("4").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
            List<UserTrack> trackWay;
            if (userId == null) {
                trackWay = trackWayDao.getUserTrackWay(startTime,endTime);
            } else {
                trackWay = trackWayDao.getTrackWay(userId, startTime, endTime);
            }
            if (trackWay.size()>0) {
                return trackWay;
            } else {
                return null;
            }
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
        }
        return null;
    }
    @Override
    public List<UserTrack> getTrackWay1(String userId, Long startTime, Long endTime) {
//        System.out.println(userId+","+startTime+","+endTime);
        List<UserTrack> trackWay;
        if (userId == null) {
            trackWay = trackWayDao.getUserTrackWay(startTime,endTime);
        } else {
            trackWay = trackWayDao.getTrackWay(userId, startTime, endTime);
        }
        if (trackWay.size()>0) {
            return trackWay;
        } else {
            return null;
        }
    }
}
