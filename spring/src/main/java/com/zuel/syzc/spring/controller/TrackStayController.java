package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.dto.OdDetail;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.OdMatrixAll;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
import com.zuel.syzc.spring.model.vo.ResultData;
import com.zuel.syzc.spring.service.TrackStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @RequestMapping("/getZoneOd")
    public  ResultData getZoneOd(Integer zone){
        ResultData<Map<String,List<Long>>> resultData = new ResultData();
        if (zone == null) {
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        Map<String,List<Long>> zoneOd = trackStayService.getZoneOd(zone);
        if (zoneOd!=null){
            resultData.setData(zoneOd);
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

    @RequestMapping("/areaDivision1")
    public ResultData areaDivision1(){
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

    @RequestMapping("/getOd1")
    public  ResultData getOd1(Long startTime,Long endTime,Integer startZone,Integer endZone){
        ResultData<List<OdDetail>> resultData = new ResultData();
//        if (startTime == null) {
//            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
//            return resultData;
//        }
        List<OdDetail> od = trackStayService.getOd1(startTime, endTime,startZone,endZone);
        if (od!=null){
            resultData.setData(od);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.DB_FIND_FAILURE);
        }
        return resultData;

    }
    @RequestMapping("/getInOutFlow1")
    public ResultData getInOutFlow1(Double longitude,Double latitude,Double radius, Long startTime,Long endTime){
        ResultData<AreaInOutFlow> resultData = new ResultData<>();
        if (longitude == null || latitude == null || radius == null ||startTime == null) {
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        AreaInOutFlow inOutFlow = trackStayService.getInOutFlow1(startTime,endTime,longitude,latitude,radius);
        if (inOutFlow != null ) {
            resultData.setData(inOutFlow);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;
    }

    @RequestMapping("/getZoneCenter")
    public ResultData getZoneCenter(){
        ResultData<List<ZoneDivision>> resultData = new ResultData<>();
        List<ZoneDivision> zoneDivisions = trackStayService.zoneCenter();
        if (zoneDivisions != null ) {
            resultData.setData(zoneDivisions);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.SERVER_ERROR);
        }
        return resultData;
    }
}
