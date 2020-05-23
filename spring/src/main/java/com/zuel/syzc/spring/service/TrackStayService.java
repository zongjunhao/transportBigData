package com.zuel.syzc.spring.service;

import com.zuel.syzc.spring.model.entity.OdMatrix;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TrackStayService {
    // 小区划分
    public void areaDivision();
    // 获取Od矩阵
    public List<OdMatrix> getOd(Long startTime, Long endTime);
    // 获取指定区域内人口流入流出量
    public void getInOutFlow();
}
