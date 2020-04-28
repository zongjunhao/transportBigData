package com.zuel.syzc.spark.data;

import com.zuel.syzc.spark.init.Init;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.collection.immutable.Nil;
import scala.math.BigDecimal;

import java.util.Iterator;

public class cleanErraticData {

//    给表增加属性：ALTER TABLE pokes ADD COLUMNS (new_col INT);
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        //广播变量
        final JavaSparkContext sc=new JavaSparkContext(sparkConf);
        Broadcast<Row> broadcast;
//        Dataset<Row> initData = new Init(spark).init() ;
//        System.out.println("initData打印啦");
//        initData.show();
//
//        //原始数据去掉imsi，lac_id，cell_id为空并且imsi不包含 # * ^ 的数据
//        Dataset<Row> cleanedData = new Init(spark).getCleanedData();
//        System.out.println("cleanedData打印啦");
//        cleanedData.show();

        Dataset<Row> filledData = new cleanErraticData(spark).getFilledData(sc);
//        filledData.groupBy("imsi").count().show();
//        System.out.println("filledData打印啦");
//        filledData.show();
//        写入csv文件
//        System.out.println("写入csv");
//        filledData.write().mode("Overwrite").option("header", "true") .csv("spark/src/main/java/com/zuel/syzc/spark/data/原始数据1");

    }

    private SparkSession spark;

    public cleanErraticData(SparkSession spark){
        this.spark = spark;
    }

    public Dataset<Row> getFilledData(JavaSparkContext sc){
        spark.read().format("csv").option("header","true").load("in/服创大赛-原始数据.csv").createOrReplaceTempView("raw_data");
        double Vamx = 32.8;//地铁最大时速118km/h
        LocationUtils location = new LocationUtils();

        //原始数据
        Dataset<Row> allData = spark.sql("select * from raw_data");
        JavaRDD<Row> javaRdd = allData.toJavaRDD();

//        Broadcast broadcast1;
//        Broadcast<Row> broadcast2 =sc.broadcast(javaRdd.take(0));
//        // 使用collect打印JavaRDD
//        for (Row row : javaRdd.collect()) {
//            broadcast1=sc.broadcast(row);
//            if(row.get(1) == null){
//                System.out.println("collect打印="+row);
//                System.out.println("空值的上一条数据"+broadcast2);
//            }
//            broadcast2=broadcast1;
//        }
//        Dataset<Row> nulldata = spark.sql("select * from raw_data where imsi is null");
//        System.out.println("nulldata打印");
//        nulldata.show();

        //遍历
//        Iterator<Row> allDataIte = allData.toLocalIterator();
//        while(allDataIte.hasNext()){
//            Row row = allDataIte.next();
////            System.out.println("row的toString"+row.toString());
//            if(row.getAs("imsi") == null){
//                System.out.println("row.getString"+row.getString(1));
//            }
//        }

        /**
         * sql语句内容
         * select timestamp,imsi,lac_id,cell_id
         * from raw_data
         * where imsi is not null
         * and lac_id is not null
         * and cell_id is not null
         * and imsi not like "%#%"
         * and imsi not like "%*%"
         * and imsi not like "%^%"
         */
        Dataset<Row> cleanedData = spark.sql("select * from raw_data " +
                "where imsi is not null and lac_id is not null and cell_id is not null " +
                "and imsi not like \"%#%\" and imsi not like \"%*%\" and imsi not like \"%^%\"");

        return cleanedData;
    }


}
