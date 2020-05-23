package com.zuel.syzc.spring.model.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CurrentCellCount {
    private String day;
    private String dayTime;
    private String cellId;
    private Long dayHour;
    private Double count;
    private Integer isAbnormal;
}
