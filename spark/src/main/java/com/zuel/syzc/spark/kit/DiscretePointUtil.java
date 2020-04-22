package com.zuel.syzc.spark.kit;


import java.util.List;

/**
 * <p>
 * <b>离散点计算工具</b>
 * <p>
 * <pre>
 * 离散点计算工具
 *
 *  y
 *  ↑   ·  ·
 *  │  · ·   ·
 *  │ ·  · ·   ·
 *  │  ·  ·
 * —│————————————→ x
 * </pre>
 *
 * @author ManerFan 2015年4月9日
 */
public class DiscretePointUtil {

    /**
     * <p>
     * <b>查找离散点集中的(minX, minY) (maxX, maxY)</b>
     * <p>
     * <pre>
     * 查找离散点集中的(minX, minY) (maxX, maxY)
     * </pre>
     *
     * @param points 离散点集
     * @return [(minX, minY) (maxX, maxY)]
     * @author ManerFan 2015年4月9日
     */
    public static BaseStationPoint[] calMinMaxDots(final List<BaseStationPoint> points) {
        if (null == points || points.isEmpty()) {
            return null;
        }

        double minX = points.get(0).getLongitude(), maxX = points.get(0).getLongitude();
        double minY = points.get(0).getLatitude(), maxY = points.get(0).getLatitude();

        /* 这里存在优化空间，可以使用并行计算 */
        for (BaseStationPoint point : points) {
            if (minX > point.getLongitude()) {
                minX = point.getLongitude();
            }

            if (maxX < point.getLongitude()) {
                maxX = point.getLongitude();
            }

            if (minY > point.getLatitude()) {
                minY = point.getLatitude();
            }

            if (maxY < point.getLatitude()) {
                maxY = point.getLatitude();
            }
        }

        BaseStationPoint ws = new BaseStationPoint(minX, minY);
        BaseStationPoint en = new BaseStationPoint(maxX, maxY);

        return new BaseStationPoint[]{ws, en};
    }

    /**
     * <p>
     * <b>求矩形面积平方根</b>
     * <p>
     * <pre>
     * 以两个点作为矩形的对角线上的两点，计算其面积的平方根
     * </pre>
     *
     * @param ws 西南点
     * @param en 东北点
     * @return 矩形面积平方根
     * @author ManerFan 2015年4月9日
     */
    public static double calRectAreaSquare(BaseStationPoint ws, BaseStationPoint en) {
        if (null == ws || null == en) {
            return .0;
        }

        /* 为防止计算面积时float溢出，先计算各边平方根，再相乘 */
        return Math.sqrt(Math.abs(ws.getLongitude() - en.getLongitude()))
                * Math.sqrt(Math.abs(ws.getLatitude() - en.getLatitude()));
    }

    /**
     * <p>
     * <b>求两点之间的长度</b>
     * <p>
     * <pre>
     * 求两点之间的长度
     * </pre>
     *
     * @param ws 西南点
     * @param en 东北点
     * @return 两点之间的长度
     * @author ManerFan 2015年4月10日
     */
    public static double calLineLen(BaseStationPoint ws, BaseStationPoint en) {
        if (null == ws || null == en) {
            return .0;
        }

        if (ws.equals(en)) {
            return .0;
        }
        // 直角三角形的直边a
        double a = Math.abs(ws.getLongitude() - en.getLongitude());
        // 直角三角形的直边b
        double b = Math.abs(ws.getLatitude() - en.getLatitude());
        // 短直边
        double min = Math.min(a, b);
        // 长直边
        double max = Math.max(a, b);

        // 为防止计算平方时float溢出，做如下转换
        // √(min²+max²) = √((min/max)²+1) * abs(max)

        double inner = min / max;
        return Math.sqrt(inner * inner + 1.0) * max;
    }

    /**
     * <p>
     * <b>求两点间的中心点</b>
     * <p>
     * <pre>
     * 求两点间的中心点
     * </pre>
     *
     * @param ws 西南点
     * @param en 东北点
     * @return 两点间的中心点
     * @author ManerFan 2015年4月10日
     */
    public static BaseStationPoint calCerter(BaseStationPoint ws, BaseStationPoint en) {
        if (null == ws || null == en) {
            return null;
        }

        return new BaseStationPoint(ws.getLongitude() + (en.getLongitude() - ws.getLongitude()) / 2.0,
                ws.getLatitude() + (en.getLatitude() - ws.getLatitude()) / 2.0);
    }

    /**
     * <p>
     * <b>计算向量角</b>
     * <p>
     * <pre>
     * 计算两点组成的向量与x轴正方向的向量角
     * </pre>
     *
     * @param s 向量起点
     * @param d 向量终点
     * @return 向量角
     * @author ManerFan 2015年4月17日
     */
    public static double angleOf(BaseStationPoint s, BaseStationPoint d) {
        double dist = calLineLen(s, d);

        if (dist <= 0) {
            return .0;
        }
        // 直角三角形的直边a
        double x = d.getLongitude() - s.getLongitude();
        // 直角三角形的直边b
        double y = d.getLatitude() - s.getLatitude();
        // 1 2 象限
        if (y >= 0) {
            return Math.acos(x / dist);
        } else { // 3 4 象限
            return Math.acos(-x / dist) + Math.PI;
        }
    }

    /**
     * <p>
     * <b>修正角度</b>
     * <p>
     * <pre>
     * 修正角度到 [0, 2PI]
     * </pre>
     *
     * @param angle 原始角度
     * @return 修正后的角度
     * @author ManerFan 2015年4月17日
     */
    public static double reviseAngle(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

}
