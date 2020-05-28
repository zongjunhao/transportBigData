package com.zuel.syzc.spring.utils;

import com.zuel.syzc.spring.model.entity.ZoneDivision;
import com.zuel.syzc.spring.service.CrowdDensityService;
import com.zuel.syzc.spring.service.TrackStayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@EnableAsync
public class MultiThreadScheduleTask {
    @Autowired
    private CrowdDensityService crowdDensityService;
    @Autowired
    private TrackStayService trackStayService;

    @Async
    @Scheduled(cron = "0 0 0 * * ?")//fixedDelay = 1000 * 60* 60* 24
    public void getHistoryModel(){
        crowdDensityService.computeHistoryModel(null,null);
        trackStayService.areaDivision(null,null);
    }
}
