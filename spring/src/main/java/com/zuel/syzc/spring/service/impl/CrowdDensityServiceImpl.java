package com.zuel.syzc.spring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.CurrentCellCountDao;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.dao.TrackWayDao;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.entity.CurrentCellCount;
import com.zuel.syzc.spring.model.entity.Task;
import com.zuel.syzc.spring.service.CrowdDensityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CrowdDensityServiceImpl implements CrowdDensityService {
    private CurrentCellCountDao currentCellCountDao;
    private SparkEntry sparkEntry = new SparkEntry();
    @Autowired
    private TaskDao taskDao;
    @Autowired
    public void setCurrentCellCountDao(CurrentCellCountDao currentCellCountDao) {
        this.currentCellCountDao = currentCellCountDao;
    }
    @Override
    public int computeHistoryModel(Long startTime,Long endTime) {
        String[] param = {String.valueOf(startTime),String.valueOf(endTime)};
        Task task = Task.builder().startTime(new Date()).taskType("5").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
        }
        return entry;
    }

    @Override
    public List<CellCrowd> getCrowdCount(Long startTime, Long endTime) {
        String[] param = {String.valueOf(startTime),String.valueOf(endTime)};
        Task task = Task.builder().startTime(new Date()).taskType("6").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
            List<CellCrowd> currentCellCounts = currentCellCountDao.select(startTime,endTime);
            if (currentCellCounts.size()>0)
                return currentCellCounts;
            else
                return null;
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
            return null;
        }
    }

    @Override
    public List<CellCrowd> getCrowdCount1(Long startTime, Long endTime) {
        List<CellCrowd> currentCellCounts = currentCellCountDao.selectAll();
        if (currentCellCounts.size()>0)
            return currentCellCounts;
        else
            return null;
    }
}
