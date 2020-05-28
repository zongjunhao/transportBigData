package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
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
        ResultData<List<ZoneDivision>> resultData = new ResultData();
        List<ZoneDivision> zoneDivisions = trackStayService.getAreaDivision();
        if (zoneDivisions.size()>0) {
            resultData.setData(zoneDivisions);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;

    }
    @RequestMapping("/getOd")
    public  ResultData getOd(Long startTime,Long endTime){
        ResultData<List<OdMatrix>> resultData = new ResultData();
        if (startTime == null) {
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        List<OdMatrix> od = trackStayService.getOd(startTime, endTime);
        if (od!=null){
            resultData.setData(od);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;

    }
    @RequestMapping("/getInOutFlow")
    public ResultData getInOutFlow(Double longitude,Double latitude,Double radius, Long startTime,Long endTime){
        ResultData<AreaInOutFlow> resultData = new ResultData<>();
        if (longitude == null || latitude == null || radius == null ||startTime == null) {
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        AreaInOutFlow inOutFlow = trackStayService.getInOutFlow(startTime,endTime,longitude,latitude,radius);
        if (inOutFlow != null ) {
            resultData.setData(inOutFlow);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;
    }

}
