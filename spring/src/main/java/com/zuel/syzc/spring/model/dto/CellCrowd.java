package com.zuel.syzc.spring.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CellCrowd {

    private String cellId;
    private Long dayHour;
    private Double count;
    private Integer isAbnormal;
    private Double longitude;
    private Double latitude;
}
