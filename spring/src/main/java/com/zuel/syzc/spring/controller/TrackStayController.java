package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.vo.ResultData;
import com.zuel.syzc.spring.service.TrackStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("stay")
public class TrackStayController {
    @Autowired
    private TrackStayService trackStayService;

    @RequestMapping("/areaDivision")
    public ResultData areaDivision(){
        ResultData<List<UserTrack>> resultData = new ResultData();
        return resultData;

    }
    @RequestMapping("/getOd")
    public  ResultData getOd(Long startTime,Long endTime){
        ResultData<List<OdMatrix>> resultData = new ResultData();
//        if (startTime == null) {
//            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
//            return resultData;
//        }
        List<OdMatrix> od = trackStayService.getOd(startTime, endTime);
        if (od!=null){
            resultData.setData(od);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.DB_FIND_FAILURE);
        }
        return resultData;

    }
    @RequestMapping("/getInOutFlow")
    public ResultData getInOutFlow(){
        ResultData resultData = new ResultData();
        return resultData;
    }

}
