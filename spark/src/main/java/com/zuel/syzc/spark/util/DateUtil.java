package com.zuel.syzc.spark.util;

import com.zuel.syzc.spark.crowd.UnusualCrowdDensity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    private static SimpleDateFormat sf = null;

    public static void main(String[] args) {
//        System.out.println(getDay(DateUtil.getDateFormat(1538544930607L)));
//        System.out.println(getDateFormat(DateUtil.getDay(DateUtil.getDateFormat(1538544930607L))));
//        System.out.println(getDay(1538544930607L));
//        System.out.println(getDateFormat(1538544930607L));
//        System.out.println(setUniqueData(1538544930607L));
//        System.out.println(getDateFormat(setUniqueData(1538544930607L)));
        long startDay = DateUtil.getTimestamp("2018-10-02 18:00:00.000");
        long endDay = DateUtil.getTimestamp("2018-10-03 23:00:00.000");
        long interval = 24 * 60 * 60 * 1000;
        long temp = startDay;
        while (temp<endDay){
            System.out.println(getDateFormat(temp)+"--"+getDateFormat(temp+interval));
//            System.out.println();
            temp += interval;
        }
//        do {
//            System.out.println(getDateFormat(temp));
//            temp += interval;
//        }while (temp<endDay);
    }

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

    public static String getDateFormatHour(Long time){
        sf = new SimpleDateFormat("yyyy-MM-dd-HH");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(time);
    }

    /**
     * 获取当前整点时间
     * @param time 时间戳
     * @return
     */
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

    public static String getDay(long time){
        sf = new SimpleDateFormat("yyyy-MM-dd");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(time);
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

    /**
     * 获取时间戳的日期
     * @param time
     * @return
     */
    public static int getWeek(long time){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        calendar.setTime(new Date(time));
        // 1星期日 2一 3二 4三 5四 6五 7六
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getHour(long time){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        calendar.setTime(new Date(time));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static boolean isAfter(String time1,String time2) {
        if (Long.parseLong(time1) > Long.parseLong(time2))
            return false;
        else
            return true;
    }

}
