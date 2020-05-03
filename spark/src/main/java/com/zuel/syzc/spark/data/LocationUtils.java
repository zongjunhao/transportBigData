package com.zuel.syzc.spark.data;

import com.zuel.syzc.spark.util.DateUtil;

public class LocationUtils {

    private static double EARTH_RADIUS = 6378.137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 距离
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;
        return s;
    }

    public static void main(String[] args) {
        /**
         * |1538481509242|460000095007329090| 16789|67567924|123.4159698|41.80778122|
         * |1538526105074|460000095007329090| 16789|67200820|123.4396362|41.80488968|
         */
        double distance = getDistance(41.80778122, 123.4159698,
                41.80488968, 123.4396362);
        System.out.println("距离" + distance + "米");
        long minus = 1538526105074L-1538481509242L;
        System.out.println(DateUtil.getDateFormat(1538526105074L));
        System.out.println(DateUtil.getDateFormat(1538481509242L));
        System.out.println(minus/(60 * 60 * 1000));
        System.out.println(distance/1000);
        System.out.println(distance/minus*60*60);
    }

}
