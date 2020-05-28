package com.zuel.syzc.spring;

import com.zuel.syzc.spark.util.DateUtil;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
import com.zuel.syzc.spring.service.CrowdDensityService;
import com.zuel.syzc.spring.service.TrackStayService;
import com.zuel.syzc.spring.service.TrackWayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CrowdDenistyTest {
    @Autowired
    CrowdDensityService crowdDensityService;
    @Autowired
    TrackStayService trackStayService;
    @Autowired
    TrackWayService trackWayService;

    @Test
    public void computeHistoryModel() {
        Long time = 1538578800000L;
        int i = crowdDensityService.computeHistoryModel(null, null);
        System.out.println("result:"+i);
    }

    @Test
    public void getCrowdCount(){
        Long time = 1538578800000L;
        long endtime = time + 1000* 60 * 60;
        List<CellCrowd> crowdCount = crowdDensityService.getCrowdCount(time, endtime);
        crowdCount.forEach(System.out::println);
    }

    @Test
    void getOd(){
        List<OdMatrix> od = trackStayService.getOd(DateUtil.getDayHour("2018-10-01-00"), DateUtil.getDayHour("2018-10-03-00"));
        od.forEach(System.out::println);
    }

    @Test
    void zoneDivision(){
        trackStayService.areaDivision(null,null);
//        zoneDivisions.forEach(System.out::println);
    }

    @Test
    void getInOutFlow(){
        AreaInOutFlow inOutFlow = trackStayService.getInOutFlow(DateUtil.getDayHour("2018-10-01-00"), DateUtil.getDayHour("2018-10-03-00"),123.41348730697675d,41.80973301441861,200d);
        System.out.println(inOutFlow);
    }

    @Test
    void getTrackWay(){
        System.out.println(DateUtil.getDayHour("2018-10-03-00")+"---"+DateUtil.getDayHour("2018-10-04-00"));
        List<UserTrack> trackWay = trackWayService.getTrackWay("460020095098364762", DateUtil.getDayHour("2018-10-03-00"), DateUtil.getDayHour("2018-10-04-00"));
        trackWay.forEach(System.out::println);
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.getDayHour("2018-10-03-12"));
    }
    @Test
    void getTime(){

    }


}
