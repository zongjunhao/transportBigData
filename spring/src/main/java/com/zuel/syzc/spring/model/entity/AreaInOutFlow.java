package com.zuel.syzc.spring.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("area_inout_flow")
public class AreaInOutFlow {
    private Long inflow;
    private Long outflow;
}
