package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.entity.ZoneDivision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ZoneDivisonDao extends BaseMapper<ZoneDivision> {
    @Select("select avg(longitude) longitude,avg(latitude) latitude,zone " +
            "from zone_division " +
            "group by zone")
    List<ZoneDivision> getCenterZone();

}
