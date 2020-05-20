package com.zuel.syzc.spark.track;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TrackStation {
    String cellId;
    Long timestamp;

    public TrackStation(String cellId, Long timestamp) {
        this.cellId = cellId;
        this.timestamp = timestamp;
    }
}
