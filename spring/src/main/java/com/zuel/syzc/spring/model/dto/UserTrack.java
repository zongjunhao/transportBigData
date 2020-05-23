package com.zuel.syzc.spring.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserTrack {
    private String userId;
    private String cellId;
    private String longitude;
    private String latitude;
    private Long timestamp;
    private String trackId;
    private String trackWay;
}
