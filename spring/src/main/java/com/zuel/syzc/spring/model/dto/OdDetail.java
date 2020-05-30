package com.zuel.syzc.spring.model.dto;

import com.zuel.syzc.spring.model.entity.OdMatrix;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OdDetail {
    private Integer startId;
    private Integer endId;
    private Integer count;
    private Point startPoint;
    private Point endPoint;

    public OdDetail(OdMatrix odMatrix, Point startPoint, Point endPoint) {
        this.startId = odMatrix.getStartId();
        this.endId = odMatrix.getEndId();
        this.count = odMatrix.getCount();
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }
}
