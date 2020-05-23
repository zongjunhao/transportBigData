package com.zuel.syzc.spark.track;

import lombok.Data;
import lombok.ToString;
import scala.Tuple4;

@Data
@ToString
public class UserTrack {
    private String userId;
    private String cellId;
    private String longitude;
    private String latitude;
    private Long timestamp;
    private String trackId;

    public UserTrack(Tuple4<Long, String, String, String> track,String userId,String trackId) {
        this.userId = userId;
        this.cellId = track._2();
        this.longitude = track._3();
        this.latitude = track._4();
        this.timestamp = track._1();
        this.trackId = trackId;
    }
}
