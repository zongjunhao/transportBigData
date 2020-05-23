package com.zuel.syzc.spring.service.impl;

import com.zuel.syzc.spring.dao.OdMatrixDao;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.service.TrackStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackStayServiceImpl implements TrackStayService {
    @Autowired
    private OdMatrixDao odMatrixDao;
    @Override
    public void areaDivision() {

    }

    @Override
    public List<OdMatrix> getOd(Long startTime,Long endTime) {
        int area = 9;
        List<OdMatrix> odMatrices = odMatrixDao.selectList(null);
        if (odMatrices.size()>0) {
            return odMatrices;
        } else {
            return null;
        }

    }

    @Override
    public void getInOutFlow() {

    }
}
