package com.zuel.syzc.spark.kit;

import com.zuel.syzc.spark.entity.BaseStationPoint;
import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import scala.Tuple6;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * <b>最小（凸）包围边界查找</b>
 * <p>
 * <pre>
 * 最小（凸）包围边界查找
 *
 * Minimum Bounding Polygon (Convex Hull; Smallest Enclosing A Set of Points)
 * <b><a href="http://alienryderflex.com/smallest_enclosing_polygon/">©2009 Darel Rex Finley.</a></b>
 *
 *  y
 *  ↑   ·  ·
 *  │  · ·   ·
 *  │ ·  · ·   ·
 *  │  ·  ·
 * —│————————————→ x
 *
 * </pre>
 *
 * @author ManerFan 2015年4月17日
 */
public class MinimumBoundingPolygon {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        TrafficZoneDivision trafficZoneDivision = new TrafficZoneDivision(spark);
        JavaRDD<Tuple6<String, Integer, Double, Double, Long, Integer>> result = trafficZoneDivision.divisionTrafficZoneByKmeans(null,null);
//        result.collect().forEach(System.out::println);
        result.groupBy(x->x._6()).foreach(x->{
            System.out.println("\n"+x._1);
            List<Tuple6<String, Integer, Double, Double, Long, Integer>> list = IteratorUtils.toList(x._2.iterator());
            list.forEach(System.out::println);
        });
//        MinimumBoundingPolygon.findSmallestPolygon()
    }

    /**
     * 边界查找入口
     * @param ps 散点列表
     * @return 边界点列表
     */
    public static LinkedList<BaseStationPoint> findSmallestPolygon(List<BaseStationPoint> ps) {
        if (null == ps || ps.isEmpty()) {
            return null;
        }

        BaseStationPoint corner = findStartPoint(ps);
        if (null == corner) {
            return null;
        }

        double minAngleDif, oldAngle = 2 * Math.PI;
        LinkedList<BaseStationPoint> bound = new LinkedList<>();
        do {
            minAngleDif = 2 * Math.PI;

            bound.add(corner);

            BaseStationPoint nextPoint = corner;
            double nextAngle = oldAngle;
            for (BaseStationPoint p : ps) {
                // 已被加入边界链表的点
                if (p.founded) {
                    continue;
                }

                // 重合点
                if (p.equals(corner)) {
                    /*if (!p.equals(bound.getFirst())) {
                        p.founded = true;
                    }*/
                    continue;
                }
                /* 当前向量与x轴正方向的夹角 */
                double currAngle = DiscretePointUtil.angleOf(corner, p);
                /* 两条向量之间的夹角（顺时针旋转的夹角） */
                double angleDif = DiscretePointUtil.reviseAngle(oldAngle - currAngle);

                if (angleDif < minAngleDif) {
                    minAngleDif = angleDif;
                    nextPoint = p;
                    nextAngle = currAngle;
                }
            }

            oldAngle = nextAngle;
            corner = nextPoint;
            corner.founded = true;
            /* 判断边界是否闭合 */
        } while (!corner.equals(bound.getFirst()));

        return bound;
    }

    /**
     * 查找起始点（保证y最大的情况下、尽量使x最小的点）
     */
    private static BaseStationPoint findStartPoint(List<BaseStationPoint> ps) {
        if (null == ps || ps.isEmpty()) {
            return null;
        }

        BaseStationPoint p = ps.get(0);

        for (BaseStationPoint point : ps) {
            // 找到最靠上靠左的点
            if (point.getLatitude() > p.getLatitude() || (point.getLatitude() == p.getLatitude() && point.getLongitude() < p.getLongitude())) {
                p = point;
            }
        }

        return p;
    }
}
