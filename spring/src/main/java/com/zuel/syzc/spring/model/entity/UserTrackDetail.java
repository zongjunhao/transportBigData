package com.zuel.syzc.spring.model.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserTrackDetail {
    private String userId;
    private String cellId;
    private String longitude;
    private String latitude;
    private Long timestamp;
    private String trackId;
}
