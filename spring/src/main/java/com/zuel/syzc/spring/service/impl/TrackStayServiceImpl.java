package com.zuel.syzc.spring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zuel.syzc.spark.SparkEntry;
import com.zuel.syzc.spring.dao.AreaInOutFlowDao;
import com.zuel.syzc.spring.dao.OdMatrixDao;
import com.zuel.syzc.spring.dao.TaskDao;
import com.zuel.syzc.spring.dao.ZoneDivisonDao;
import com.zuel.syzc.spring.model.dto.OdDetail;
import com.zuel.syzc.spring.model.dto.Point;
import com.zuel.syzc.spring.model.entity.*;
import com.zuel.syzc.spring.service.TrackStayService;
import org.apache.commons.net.ftp.parser.MacOsPeterFTPEntryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<ZoneDivision> zoneCenter() {
        List<ZoneDivision> centerZone = zoneDivisonDao.getCenterZone();
        if (centerZone.size()>0) {
            return centerZone;
        }
        return null;
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
    public Map<String,List<Long>> getZoneOd(Integer zone) {
        List<OdMatrixAll> zoneInOutFlowDay = odMatrixDao.getZoneInOutFlowDay(zone);
        if (zoneInOutFlowDay.size()>0) {
            List<Long> inflow = zoneInOutFlowDay.stream().map(OdMatrixAll::getInflow).collect(Collectors.toList());
            List<Long> outFlow = zoneInOutFlowDay.stream().map(OdMatrixAll::getOutflow).collect(Collectors.toList());
            Map<String,List<Long>> result = new HashMap<>();
            result.put("inflow",inflow);
            result.put("outflow",outFlow);
            return result;
        }
        return null;
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

    @Override
    public List<ZoneDivision> getAreaDivision1() {
        List<ZoneDivision> zoneDivisions = zoneDivisonDao.selectList(null);
        if (zoneDivisions.size()>0) {
            return zoneDivisions;
        } else {
            return null;
        }
    }

    @Override
    public List<OdDetail> getOd1(Long startTime,Long endTime,Integer startZone,Integer endZone) {
//        int area = 9;


        QueryWrapper<OdMatrix> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(startZone!=null,"start_id",startZone);
        queryWrapper.eq(endZone!=null,"end_id",endZone);
        List<OdMatrix> odMatrices = odMatrixDao.selectList(queryWrapper);

        if (odMatrices.size()>0) {
            List<ZoneDivision> centerZone = zoneDivisonDao.getCenterZone();
            Map<Integer, Point> zoneMap = centerZone.stream().collect(Collectors.toMap(ZoneDivision::getZone, v -> new Point(v.getLongitude(), v.getLatitude())));
            zoneMap.forEach((k,v)->System.out.println(k+"---"+v));
            List<OdDetail> odDetail = odMatrices.stream().map(x -> {
                Point startPoint = zoneMap.get(x.getStartId());
                Point endPoint = zoneMap.get(x.getEndId());
                return new OdDetail(x, startPoint, endPoint);
            }).collect(Collectors.toList());
            return odDetail;
        } else {
            return null;
        }
    }

    @Override
    public AreaInOutFlow getInOutFlow1(Long startTime,Long endTime,double longitude, double latitude, double radius) {
        List<AreaInOutFlow> areaInOutFlows = areaInOutFlowDao.selectList(null);
        if (areaInOutFlows.size() == 1) {
            return areaInOutFlows.get(0);
        } else {
            return null;
        }
    }


}
