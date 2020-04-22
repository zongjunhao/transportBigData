package com.zuel.syzc.spark.test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.*;
import scala.Tuple2;
import static org.apache.spark.sql.functions.*;
import java.util.ArrayList;
import java.util.List;

public class TestClass {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.read().format("csv").option("header", "true").load("in/服创大赛-基站经纬度数据.csv").createOrReplaceTempView("longitude");
        Dataset<Row> dataset = spark.sql("select * from longitude");
        // dataset.show();
        // dataset.foreach(x -> {
        //     System.out.println(x.toString());
        // });
        //
        // Dataset<BaseStationPoint> points = dataset.map(
        //         (MapFunction<Row, BaseStationPoint>) (Row row) ->
        //                 new BaseStationPoint(Double.parseDouble(row.getString(0)), Double.parseDouble(row.getString(1)), row.getString(2)),
        //         Encoders.bean(BaseStationPoint.class));
        // points.show();
        // points.foreach(x -> {
        //     System.out.println(x.toString());
        // });

        // dataset.write();
        // Dataset<BaseStationPoint> baseStationPointDataset = dataset.as(Encoders.bean(BaseStationPoint.class));
        // baseStationPointDataset.show();
        // baseStationPointDataset.foreach(x -> {
        //     System.out.println(x.toString());
        // });

        Dataset<Row> newDataSet = spark.sql("set latitude = '111'");
        newDataSet.show();
    }

    @org.junit.Test
    public void unionTest() {
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("test");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<Row> dataset = spark.read()
                .option("multiLine", true).option("mode", "PERMISSIVE")
                .json("test_data/test_json.json");
        JavaSparkContext sparkContext = new JavaSparkContext(spark.sparkContext());

        List<Tuple2<String, String>> rddList = new ArrayList<>();
        rddList.add(new Tuple2<>("Michale", "111"));
        rddList.add(new Tuple2<>("Justin", "333"));
        rddList.add(new Tuple2<>("Andy", "222"));
        JavaPairRDD<String, String> rdd = sparkContext.parallelizePairs(rddList);
        JavaRDD<Row> javaRDD = rdd.map((Function<Tuple2<String, String>, Row>) tuple2 -> RowFactory.create(tuple2._1, tuple2._2));

        // 定义转化模式
        StructField[] structFields = {
                new StructField("names", DataTypes.StringType, true, Metadata.empty()),
                new StructField("num", DataTypes.StringType, true, Metadata.empty())
        };
        StructType schema = new StructType(structFields);

        List<Row> rows = new ArrayList<>();
        rows.add(RowFactory.create("Michale", "111"));
        rows.add(RowFactory.create("Justin", "333"));
        rows.add(RowFactory.create("Andy", "222"));

        Dataset<Row> secondDataset = spark.createDataFrame(javaRDD, schema);
        Dataset<Row> thirdDataset = spark.createDataFrame(rows, schema);
        dataset.show();
        secondDataset.show();
        thirdDataset.show();

        // inner, cross, outer, full, full_outer, left, left_outer, right, right_outer, left_semi, left_anti.
        Dataset<Row> result = dataset.join(secondDataset, col("name").equalTo(col("names")), "left_outer");
        result.show();

        dataset.createOrReplaceTempView("first_table");
        secondDataset.createOrReplaceTempView("second_table");
        spark.sql("select * from first_table left join second_table on first_table.name = second_table.names").show();
    }
}
