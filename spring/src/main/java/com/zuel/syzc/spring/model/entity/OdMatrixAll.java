package com.zuel.syzc.spring.model.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OdMatrixAll {
    private Integer zone;
    private long timestamp;
    private long inflow;
    private long outflow;
}
