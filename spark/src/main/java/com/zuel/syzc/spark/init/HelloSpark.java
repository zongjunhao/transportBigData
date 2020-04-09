package com.zuel.syzc.spark.init;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class HelloSpark {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.read().format("csv").option("header","true").load("in/服创大赛-原始数据.csv").createOrReplaceTempView("raw_data");
        spark.sql("select * from raw_data").show();
        spark.read().format("csv").option("header","true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
        spark.sql("select * from longitude").show();
        spark.read().format("csv").option("header","true").load("in/服创大赛-出行方式静态数据.csv").createOrReplaceTempView("way");
        spark.sql("select * from way").show();
    }
}
