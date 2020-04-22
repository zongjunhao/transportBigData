package com.zuel.syzc.spark.kit;

import java.util.List;

/**
 * 不规则多边形的外包矩形，初步过滤不在矩形内的点
 * @author zongjunhao
 */
public class Rectangle {
    private double top;
    private double bottom;
    private double left;
    private double right;

    /**
     * 根据输入的多边形顶点构造多边形的外包矩形
     *
     * @param points 多边形顶点
     */
    public Rectangle(List<BaseStationPoint> points) {
        for (BaseStationPoint point : points) {
            if (point.getLatitude() > top) {
                top = point.getLatitude();
            }
            if (point.getLatitude() < bottom) {
                bottom = point.getLatitude();
            }
            if (point.getLongitude() < left) {
                left = point.getLongitude();
            }
            if (point.getLongitude() > right) {
                right = point.getLongitude();
            }
        }
    }

    /**
     * 判断点是否在矩形内
     *
     * @param point 要判断的点
     * @return 是否在矩形内
     */
    public boolean isPointInRectangle(BaseStationPoint point) {
        return bottom < point.getLatitude() && top > point.getLatitude()
                && left < point.getLongitude() && right > point.getLongitude();
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "top=" + top +
                ", bottom=" + bottom +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
