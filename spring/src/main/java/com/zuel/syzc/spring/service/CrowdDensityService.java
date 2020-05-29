package com.zuel.syzc.spring.service;

import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.entity.CurrentCellCount;

import java.util.List;

public interface CrowdDensityService {
    // 计算历史模型
    public int computeHistoryModel(Long startTime,Long endTime);

    // 获取人群分布情况
    public List<CellCrowd> getCrowdCount(Long startTime, Long endTime); // 计算历史模型

    // 获取人群分布情况
    public List<CellCrowd> getCrowdCount1(Long startTime, Long endTime);

}
