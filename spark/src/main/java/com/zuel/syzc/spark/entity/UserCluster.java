package com.zuel.syzc.spark.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserCluster {
    private String userId;
    private String cellId;
    private String longitude;
    private String latitude;
    private Long timestamp;
    private Integer cluster;

    public UserCluster(String userId, String cellId, String longitude, String latitude, Long timestamp, Integer cluster) {
        this.userId = userId;
        this.cellId = cellId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.cluster = cluster;
    }
}
