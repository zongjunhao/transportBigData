package com.zuel.syzc.spark.test;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

public class DoTest {
    private SparkSession spark;
    public DoTest(SparkSession spark) {
        this.spark = spark;
    }

    public void doTest(){
        Row row = RowFactory.create(6666L, 88888L);
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());
        StructType schema = new StructType(new StructField[]{
                new StructField("inflow", DataTypes.LongType, true, Metadata.empty()),
                new StructField("outflow", DataTypes.LongType, true, Metadata.empty()),
        });
        List<Row> list = new ArrayList<>();
        list.add(row);
        JavaRDD<Row> parallelize = sc.parallelize(list);
        Dataset<Row> dataFrame = spark.createDataFrame(parallelize, schema);
        dataFrame.write().format("jdbc").mode(SaveMode.Append)
                .option("url", "jdbc:mysql://106.15.251.188:3306/transport_big_data")
                .option("driver", "com.mysql.jdbc.Driver")
                .option("dbtable", "test_table")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();
    }
}
