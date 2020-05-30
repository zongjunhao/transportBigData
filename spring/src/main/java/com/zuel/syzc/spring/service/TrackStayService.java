package com.zuel.syzc.spring.service;

import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TrackStayService {
    // 小区划分
    public int areaDivision(Long startTime,Long endTime);
    public List<ZoneDivision> zoneCenter();

    public List<ZoneDivision> getAreaDivision();
    // 获取Od矩阵
    public List<OdMatrix> getOd(Long startTime, Long endTime);
    // 获取指定区域内人口流入流出量
    public AreaInOutFlow getInOutFlow(Long startTime,Long endTime,double longitude, double latitude, double radius); // 小区划分

    public List<ZoneDivision> getAreaDivision1();
    // 获取Od矩阵
    public List<OdMatrix> getOd1(Long startTime, Long endTime);
    // 获取指定区域内人口流入流出量
    public AreaInOutFlow getInOutFlow1(Long startTime,Long endTime,double longitude, double latitude, double radius);
}
