package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.UserTrackDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface UserTrackDetailDao extends BaseMapper<UserTrackDetail> {
    @Select("select distinct user_id " +
            "from user_track_detail " +
            "GROUP BY user_id")
    List<String> getAllUser();
}
