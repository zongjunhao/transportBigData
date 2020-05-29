package com.zuel.syzc.spring.service;

import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.TrackWay;

import java.util.List;

public interface TrackWayService {
    // 获取出行方式表
    public List<UserTrack> getTrackWay(String userId, Long startTime, Long endTime);
    public List<UserTrack> getTrackWay1(String userId, Long startTime, Long endTime);
}
