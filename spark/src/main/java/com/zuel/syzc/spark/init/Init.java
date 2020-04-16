package com.zuel.syzc.spark.init;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Init {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<Row> cleanedData = new Init(spark).init();
        cleanedData.show();
    }
    private SparkSession spark;

    public Init(SparkSession spark){
        this.spark = spark;
    }

    public Dataset<Row> getCleanedData(){
        spark.read().format("csv").option("header","true").load("in/服创大赛-原始数据.csv").createOrReplaceTempView("raw_data");
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
        Dataset<Row> cleanedData = spark.sql("select timestamp,imsi,lac_id,cell_id from raw_data " +
                "where imsi is not null and lac_id is not null and cell_id is not null " +
                "and imsi not like \"%#%\" and imsi not like \"%*%\" and imsi not like \"%^%\"");
        return cleanedData;
    }
    /**
     *  初始化数据
     *  1. 对原始数据进行数据清洗
     *  2. 将清洗后数据与基站经纬度数据进行合并
     * @return 进行清洗和合并以后的数据表
     */
    public Dataset<Row> init(){
        spark.read().format("csv").option("header","true").load("in/服创大赛-原始数据.csv").createOrReplaceTempView("raw_data");
//    spark.sql("select * from raw_data").show()
        spark.read().format("csv").option("header","true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
//    spark.sql("select * from longitude").show()
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
        spark.sql("select timestamp,imsi,lac_id,cell_id from raw_data " +
                "where imsi is not null and lac_id is not null and cell_id is not null " +
                "and imsi not like \"%#%\" and imsi not like \"%*%\" and imsi not like \"%^%\"")
                .createOrReplaceTempView("cleaned_data"); // 创建一个表 cleaned_data存放清洗后的原始数据
        /**
         * sql语句内容
         * select timestamp,imsi,place.lac_id,place.cell_id,longitude,latitude
         * from raw_data inner join (
         * select
         * substring_index(laci,'-',1) as lac_id,
         * substring_index(laci,'-',-1) as cell_id,
         * longitude,latitude
         * from longitude) as place
         * on raw_data.lac_id = place.lac_id and raw_data.cell_id = place.cell_id
         */
        Dataset<Row> joinedData = spark.sql("select timestamp,imsi,place.lac_id,place.cell_id,longitude,latitude " +
                "from cleaned_data inner join (select substring_index(laci,'-',1) as lac_id,substring_index(laci,'-',-1) as cell_id,longitude,latitude " +
                "from longitude) as place on cleaned_data.lac_id = place.lac_id and cleaned_data.cell_id = place.cell_id");
        // 创建一个表 joined_data存放清洗后的
        joinedData.createOrReplaceTempView("joined_data");
        return joinedData;
    }
}
