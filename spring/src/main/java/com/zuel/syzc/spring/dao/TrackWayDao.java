package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.dto.UserTrack;
import com.zuel.syzc.spring.model.entity.TrackWay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TrackWayDao extends BaseMapper<TrackWay> {
    @Select("select user_id,cell_id,longitude,latitude,timestamp,user_track_detail.track_id,track_way " +
            "from user_track_detail inner join track_way on user_track_detail.track_id = track_way.track_id " +
            "where `timestamp` between ${startTime} and ${endTime}")
    List<UserTrack> getUserTrackWay(Long startTime, Long endTime);

    @Select("select user_id,cell_id,longitude,latitude,timestamp,user_track_detail.track_id,track_way " +
            "from user_track_detail inner join track_way on user_track_detail.track_id = track_way.track_id " +
            "where user_id = #{userId} and `timestamp` between ${startTime} and ${endTime}")
    List<UserTrack> getTrackWay(String userId, Long startTime, Long endTime);
}
