package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.vo.ResultData;
import com.zuel.syzc.spring.service.TrackWayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("way")
public class TrackWayController {
    @Autowired
    private TrackWayService trackWayService;

    @RequestMapping("/getTrackWay")
    public ResultData getTrackWay(String userId,Long startTime,Long endTime) {
        ResultData<List<UserTrack>> resultData = new ResultData();
        if (userId ==null || startTime == null) {
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        if (endTime == null) {
            endTime = startTime + 1000 * 60 * 60 * 24;
        }
        List<UserTrack> trackWay = trackWayService.getTrackWay(userId, startTime, endTime);
        if (trackWay!=null){
            resultData.setData(trackWay);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;
    }
}
