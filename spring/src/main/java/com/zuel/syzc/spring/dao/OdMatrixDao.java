package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.OdMatrix;
import com.zuel.syzc.spring.model.entity.OdMatrixAll;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OdMatrixDao extends BaseMapper<OdMatrix> {
    @Select("select * from od_matrix_all where zone = ${zone} order by timestamp")
    List<OdMatrixAll> getZoneInOutFlowDay(Integer zone);
}
