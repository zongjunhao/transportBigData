package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.AreaInOutFlow;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface AreaInOutFlowDao extends BaseMapper<AreaInOutFlow> {
}
