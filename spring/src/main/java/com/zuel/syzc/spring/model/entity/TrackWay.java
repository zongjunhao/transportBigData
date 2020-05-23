package com.zuel.syzc.spring.model.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TrackWay {
    private String trackId;
    private String startCell;
    private Long startTimestamp;
    private String endCell;
    private Long endTimestamp;
    private String trackWay;
}
