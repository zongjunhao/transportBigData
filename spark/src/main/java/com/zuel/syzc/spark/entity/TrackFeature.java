package com.zuel.syzc.spark.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TrackFeature {
    // avgV,distance,midV,p95V,minV
    private String trackId;
    private double avgV;
    private double distance;
    private double midV;
    private double p95v;
    private double minV;

    public TrackFeature() {
    }

    public TrackFeature(String trackId,double avgV, double distance, double midV, double p95v, double minV) {
        this.trackId = trackId;
        this.avgV = avgV;
        this.distance = distance;
        this.midV = midV;
        this.p95v = p95v;
        this.minV = minV;
    }
}
