package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.UserTrackDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserTrackDetailDao extends BaseMapper<UserTrackDetail> {
}
