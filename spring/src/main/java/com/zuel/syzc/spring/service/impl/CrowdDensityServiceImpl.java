package com.zuel.syzc.spring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zuel.syzc.spring.dao.CurrentCellCountDao;
import com.zuel.syzc.spring.dao.TrackWayDao;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.entity.CurrentCellCount;
import com.zuel.syzc.spring.service.CrowdDensityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrowdDensityServiceImpl implements CrowdDensityService {
    private CurrentCellCountDao currentCellCountDao;
    @Autowired
    public void setCurrentCellCountDao(CurrentCellCountDao currentCellCountDao) {
        this.currentCellCountDao = currentCellCountDao;
    }

    @Override
    public void computeHistoryModel() {

    }

    @Override
    public List<CellCrowd> getCrowdCount(Long startTime, Long endTime) {
//        QueryWrapper<CurrentCellCount> queryWrapper = new QueryWrapper<>();
//        System.out.println(startTime+"-"+endTime);
//        queryWrapper.between("day_hour",startTime.toString(),endTime.toString());
        List<CellCrowd> currentCellCounts = currentCellCountDao.select(startTime,endTime);
        if (currentCellCounts.size()>0)
            return currentCellCounts;
        else
            return null;
    }
}
