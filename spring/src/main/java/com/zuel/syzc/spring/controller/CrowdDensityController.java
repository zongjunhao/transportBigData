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

    public void computeHistoryModel(){

    }

    @RequestMapping("/getCrowdCount")
    public ResultData getCrowdCount(Long startTime, Long endTime){
        ResultData<List<CellCrowd>> resultData = new ResultData<>();
        if (startTime == null) {
            startTime = 1538578800000L;
//            resultData.setResult(ResultCodeEnum.PARA_WORNING_NULL);
//            return resultData;
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
}
