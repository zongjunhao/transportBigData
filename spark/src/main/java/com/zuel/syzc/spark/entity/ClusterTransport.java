package com.zuel.syzc.spark.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ClusterTransport implements Comparable<ClusterTransport>{
    private int cluster;
    private double avgV;

    @Override
    public int compareTo(ClusterTransport o) {
        return Double.compare(avgV,o.getAvgV());
    }
}
