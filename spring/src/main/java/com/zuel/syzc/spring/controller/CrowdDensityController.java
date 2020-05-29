package com.zuel.syzc.spring.controller;

import com.zuel.syzc.spring.constant.enums.ResultCodeEnum;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.entity.CurrentCellCount;
import com.zuel.syzc.spring.model.vo.ResultData;
import com.zuel.syzc.spring.service.CrowdDensityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/crowd")
public class CrowdDensityController {

    private CrowdDensityService crowdDensityService;
    @Autowired
    public void setCrowdDensityService(CrowdDensityService crowdDensityService) {
        this.crowdDensityService = crowdDensityService;
    }

    @RequestMapping("/computeHistoryModel")
    public ResultData computeHistoryModel(Long startTime,Long endTime){
        ResultData resultData = new ResultData();
        int result;
//        if (endTime == null) {
//            endTime = System.currentTimeMillis();
//        }
//        if (startTime == null) {
//            result = crowdDensityService.computeHistoryModel(null,endTime);
//        } else {
        result = crowdDensityService.computeHistoryModel(startTime,endTime);
//        }
        switch (result) {
            case 0:
                resultData.setResult(ResultCodeEnum.OK);
                break;
            case -1:
                resultData.setResult(ResultCodeEnum.SERVER_ERROR);
            case -2:
            case -3:
                resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            default:
                resultData.setResult(ResultCodeEnum.UNKOWN_ERROE);
        }
        return resultData;
    }

    @RequestMapping("/getCrowdCount")
    public ResultData getCrowdCount(Long startTime, Long endTime){
        ResultData<List<CellCrowd>> resultData = new ResultData<>();
        if (startTime == null) {
//            startTime = 1538578800000L;
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        if (endTime == null) {
            endTime = startTime + 1000*60*60;
        }
        List<CellCrowd> crowdCount = crowdDensityService.getCrowdCount(startTime, endTime);
        if (crowdCount!=null){
            resultData.setData(crowdCount);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.DB_FIND_FAILURE);
        }
        return resultData;
    }

    @RequestMapping("/getCrowdCount1")
    public ResultData getCrowdCount1(Long startTime, Long endTime){
        ResultData<List<CellCrowd>> resultData = new ResultData<>();
        if (startTime == null) {
//            startTime = 1538578800000L;
            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
            return resultData;
        }
        if (endTime == null) {
            endTime = startTime + 1000*60*60;
        }
        List<CellCrowd> crowdCount = crowdDensityService.getCrowdCount1(startTime, endTime);
        if (crowdCount!=null){
            resultData.setData(crowdCount);
            resultData.setResult(ResultCodeEnum.OK);
        } else {
            resultData.setResult(ResultCodeEnum.DB_FIND_FAILURE);
        }
        return resultData;
    }
}
