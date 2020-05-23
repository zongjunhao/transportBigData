package com.zuel.syzc.spring.service.impl;

import com.zuel.syzc.spring.dao.TrackWayDao;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.TrackWay;
import com.zuel.syzc.spring.service.TrackWayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackWayServiceImpl implements TrackWayService {
    @Autowired
    private TrackWayDao trackWayDao;

    @Override
    public List<UserTrack> getTrackWay(String userId, Long startTime, Long endTime) {
//        System.out.println(userId+","+startTime+","+endTime);
        List<UserTrack> trackWay = trackWayDao.getTrackWay(userId, startTime, endTime);
        if (trackWay.size()>0) {
            return trackWay;
        }
        return null;
    }
}
