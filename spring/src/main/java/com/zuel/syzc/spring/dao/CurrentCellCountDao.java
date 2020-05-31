package com.zuel.syzc.spring.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuel.syzc.spring.model.dto.CellCrowd;
import com.zuel.syzc.spring.model.entity.CurrentCellCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CurrentCellCountDao extends BaseMapper<CurrentCellCount> {

    @Select("select current_cell_count.cell_id,count,is_abnormal,longitude,latitude,`day_hour` " +
            "from current_cell_count inner join cell_base on current_cell_count.cell_id = cell_base.cell_id where `day_hour` BETWEEN ${startTime} and ${endTime}")
    List<CellCrowd> select(Long startTime, Long endTime);
    @Select("select current_cell_count.cell_id,count,is_abnormal,longitude,latitude,`day_hour` " +
            "from current_cell_count inner join cell_base on current_cell_count.cell_id = cell_base.cell_id")
    List<CellCrowd> selectAll();
}
