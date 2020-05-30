package com.zuel.syzc.spring.model.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OdAll {
    private String communityId;
    private Long timestamp;
    private Long inflow;
    private Long outflow;
}
