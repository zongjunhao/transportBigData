package com.zuel.syzc.spark.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    private static SimpleDateFormat sf = null;

    /**
     * 获取系统时间
     * @return
     */
    public static String getCurrentDate(){
        Date d = new Date();
        sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(d);
    }

    /**
     * 将时间戳转化为时间格式,精确到毫秒
     * yyyy-MM-dd HH:mm:ss.SSS
     * @param time
     * @return
     */
    public static String getDateFormat(String time){
        sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(Long.parseLong(time));
    }

    /**
     * 将时间戳转化为时间格式,精确到毫秒
     * yyyy-MM-dd HH:mm:ss.SSS
     * @param time
     * @return
     */
    public static String getDateFormat(Long time){
        sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(time);
    }

    public static long setUniqueData(Long time){
        sf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return getDayHour(sf.format(time));
    }

    /**
     * 将某一天时间转化为时间戳
     * yyyy-MM-dd
     * @param time
     * @return 那一天的时间戳
     */
    public static Long getDay(String time){
        sf = new SimpleDateFormat("yyyy-MM-dd");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 将某一天某时的时间转化为时间戳
     * yyyy-MM-dd-HH
     * @param time
     * @return 那一天的时间戳
     */
    public static Long getDayHour(String time){
        sf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }


    /**
     * 将格式化后的数据转化为时间戳
     * @param time
     * @return
     */
    public static long getTimestamp(String time){
        sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static boolean isAfter(String time1,String time2) {
        if (Long.parseLong(time1) > Long.parseLong(time2))
            return false;
        else
            return true;
    }

}
