package com.zuel.syzc.spring.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Point{
    Double longitude;
    Double latitude;

    public Point(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
