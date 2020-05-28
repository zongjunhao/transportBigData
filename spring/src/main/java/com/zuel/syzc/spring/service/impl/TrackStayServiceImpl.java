package com.zuel.syzc.spring.service.impl;

import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.AreaInOutFlowDao;
import com.zuel.syzc.spring.dao.OdMatrixDao;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.dao.ZoneDivisonDao;
import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.Task;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
import com.zuel.syzc.spring.service.TrackStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TrackStayServiceImpl implements TrackStayService {
    @Autowired
    private OdMatrixDao odMatrixDao;
    @Autowired
    private AreaInOutFlowDao areaInOutFlowDao;
    @Autowired
    private ZoneDivisonDao zoneDivisonDao;
    @Autowired
    private TaskDao taskDao;
    private SparkEntry sparkEntry = new SparkEntry();

    @Override
    public int areaDivision(Long startTime,Long endTime) {
        String[] param = {String.valueOf(startTime),String.valueOf(endTime)};
        Task task = Task.builder().startTime(new Date()).taskType("1").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
//            return null;
        }
        return entry;
    }

    @Override
    public List<ZoneDivision> getAreaDivision() {
        List<ZoneDivision> zoneDivisions = zoneDivisonDao.selectList(null);
        if (zoneDivisions.size()>0) {
            return zoneDivisions;
        } else {
            return null;
        }
    }

    @Override
    public List<OdMatrix> getOd(Long startTime,Long endTime) {
//        int area = 9;
        String[] param = {String.valueOf(startTime),String.valueOf(endTime)};
        Task task = Task.builder().startTime(new Date()).taskType("2").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
            List<OdMatrix> odMatrices = odMatrixDao.selectList(null);
            if (odMatrices.size()>0) {
                return odMatrices;
            } else {
                return null;
            }
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
            return null;
        }
    }

    @Override
    public AreaInOutFlow getInOutFlow(Long startTime,Long endTime,double longitude, double latitude, double radius) {
        String[] param = {String.valueOf(startTime),String.valueOf(endTime),String.valueOf(longitude),String.valueOf(latitude),String.valueOf(radius)};
        Task task = Task.builder().startTime(new Date()).taskType("3").params(String.join("#", param)).status("unfinished").build();
        taskDao.insert(task);
        int entry = sparkEntry.entry(new String[]{task.getTaskType(), task.getParams()});
        task.setEndTime(new Date());
        if (entry == 0) {
            task.setStatus("finished");
            taskDao.updateById(task);
            List<AreaInOutFlow> areaInOutFlows = areaInOutFlowDao.selectList(null);
            if (areaInOutFlows.size() == 1) {
                return areaInOutFlows.get(0);
            } else {
                return null;
            }
        } else {
            task.setStatus("error");
            taskDao.updateById(task);
            return null;
        }
    }
}
