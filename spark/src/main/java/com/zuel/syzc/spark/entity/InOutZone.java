package com.zuel.syzc.spark.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class InOutZone {
    private Integer zone;
    private long timestamp;
    private long inflow;
    private long outflow;

    public InOutZone(Integer zone, long timestamp, long inflow, long outflow) {
        this.zone = zone;
        this.timestamp = timestamp;
        this.inflow = inflow;
        this.outflow = outflow;
    }
}
