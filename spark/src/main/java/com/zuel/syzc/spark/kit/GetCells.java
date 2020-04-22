package com.zuel.syzc.spark.kit;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import scala.Tuple2;

import java.util.*;

public class GetCells {

    /**
     * 判断基站坐标是否在圆形范围内
     *
     * @param longitude 中心点经度
     * @param latitude  中心点纬度小区划分
     * @param radius    范围半径
     * @return Tuple2<基站CellId, 位置标记> 0:不在范围内, 1:在范围内
     */
    public List<Tuple2<String, Integer>> getCellsInCircle(double longitude, double latitude, double radius) {
        Iterator<Row> data = getData();
        List<Tuple2<String, Integer>> baseStationCells = new ArrayList<>();
        while (data.hasNext()) {
            Row row = data.next();
            double baseStationLongitude = Double.parseDouble(row.getString(0));
            double baseStationLatitude = Double.parseDouble(row.getString(1));
            String cellId = row.getString(2).split("-")[1];
            double distance = getDistanceBetweenPoints(baseStationLongitude, baseStationLatitude, longitude, latitude);
            if (distance < radius) {
                baseStationCells.add(new Tuple2<>(cellId, 1));
            } else {
                baseStationCells.add(new Tuple2<>(cellId, 0));
            }
        }
        System.out.println("baseStationCells.size() = " + baseStationCells.size());
        return baseStationCells;
    }

    /**
     * 计算两个经纬度坐标点之间的距离
     *
     * @param lon1 点1经度
     * @param lat1 点1纬度
     * @param lon2 点2经度
     * @param lat2 点2纬度
     * @return 两点之间距离
     */
    public double getDistanceBetweenPoints(double lon1, double lat1, double lon2, double lat2) {
        GlobalCoordinates source = new GlobalCoordinates(lat1, lon1);
        GlobalCoordinates target = new GlobalCoordinates(lat2, lon2);
        // 创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GeodeticCurve geodeticCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.WGS84, source, target);
        return geodeticCurve.getEllipsoidalDistance();
    }

    /**
     * 判断基站坐标是否在多边形范围内
     *
     * @param points 多边形顶点
     * @return Tuple2<基站CellId, 位置标记> 0:不在范围内, 1:在范围内
     */
    public List<Tuple2<String, Integer>> getCellsInPolygon(List<BaseStationPoint> points) {
        Iterator<Row> data = getData();
        List<Tuple2<String, Integer>> baseStationCells = new ArrayList<>();
        // 构造多边形的外包矩形
        Rectangle rectangle = new Rectangle(points);
        while (data.hasNext()) {
            Row row = data.next();
            double baseStationLongitude = Double.parseDouble(row.getString(0));
            double baseStationLatitude = Double.parseDouble(row.getString(1));
            String cellId = row.getString(2).split("-")[1];
            BaseStationPoint baseStation = new BaseStationPoint(baseStationLongitude, baseStationLatitude);
            // 如果基站点不在多边形的外包矩形内，则继续遍历下一个基站点，减少计算量
            if (!rectangle.isPointInRectangle(baseStation)) {
                baseStationCells.add(new Tuple2<>(cellId, 0));
                continue;
            }
            if (isPointInPolygon(baseStation, points)) {
                baseStationCells.add(new Tuple2<>(cellId, 1));
            } else {
                baseStationCells.add(new Tuple2<>(cellId, 0));
            }

        }
        System.out.println("baseStationCells.size() = " + baseStationCells.size());
        return baseStationCells;
    }

    /**
     * 使用射线法判断点是否在多边形范围内，以要判断的点为起点画一条任意方向的射线（此处为向左），
     * 与多边形边相交奇数次则点在多边形内，相交偶数次则点不在多边形内。
     *
     * @param point  要判断的点
     * @param points 多边形顶点
     * @return 是否在多边形内
     */
    private boolean isPointInPolygon(BaseStationPoint point, List<BaseStationPoint> points) {
        int pointsNum = points.size();
        // 点的数量小于3无法构成多边形
        if (pointsNum < 3) {
            return false;
        }

        // 射线与多边形边相交次数，相交奇数次为True，表示点在多边形范围内
        boolean flag = false;
        for (int i = 0; i < pointsNum; i++) {
            // 设置边的起始点
            BaseStationPoint startPoint = points.get(i);
            BaseStationPoint endPoint;
            if (i == pointsNum - 1) {
                endPoint = points.get(0);
            } else {
                endPoint = points.get(i + 1);
            }

            // 点与多边形顶点重合，判定点在多边形内
            if ((point.getLongitude() == startPoint.getLongitude() && point.getLatitude() == startPoint.getLatitude()) ||
                    (point.getLongitude() == endPoint.getLongitude() && point.getLatitude() == endPoint.getLatitude())) {
                return true;
            }

            // 判断点是否在两端点的纬度之间，在则可能有交点
            if ((endPoint.getLatitude() < point.getLatitude() && point.getLatitude() <= startPoint.getLatitude()) ||
                    (endPoint.getLatitude() >= point.getLatitude() && point.getLatitude() > startPoint.getLatitude())) {
                double longitudeOfCrossPoint = 0;
                // 起点和终点的纬度不同
                if (Math.abs(startPoint.getLatitude() - endPoint.getLatitude()) > 0) {
                    // 计算射线与线段的交点，由坐标系计算得出（计算过两点的直线与水平线的交点）
                    longitudeOfCrossPoint = endPoint.getLongitude() - (endPoint.getLatitude() - point.getLatitude())
                            * (endPoint.getLongitude() - startPoint.getLongitude())
                            / (endPoint.getLatitude() - startPoint.getLatitude());
                }
                // 点在边上，判定在多边形内
                if (longitudeOfCrossPoint == point.getLongitude()) {
                    return true;
                }
                // 水平线与线段的交点在点的左侧
                if (longitudeOfCrossPoint < point.getLongitude()) {
                    flag = !flag;
                }
            }
        }
        return flag;
    }

    /**
     * 从CSV文件中读取基站数据
     *
     * @return Iterator形式的数据
     */
    private Iterator<Row> getData() {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.read().format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
        Dataset<Row> dataset = spark.sql("select * from longitude");
        return dataset.toLocalIterator();
    }

    /**
     * 暂定中心点123.4159698,41.80778122,16789-67567924
     *
     * @param points
     */
    public void getClusterBorder(Dataset<Row> points) {
        Iterator<Row> pointsIterator = points.toLocalIterator();
        KMeans kMeans = new KMeans();
        JavaRDD<Row> rdd = points.toJavaRDD();
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.read().format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
        Dataset<Row> dataset = spark.sql("select * from longitude");
        JavaRDD<Row> javaRdd = dataset.toJavaRDD();

    }
}
