import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.fitting.leastsquares.EvaluationRmsChecker;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.parquet.it.unimi.dsi.fastutil.longs.LongComparator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.network.util.LimitedInputStream;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import scala.Tuple3;

import java.io.DataOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class Init {
    public static void main(String[] args) {
//        long timestamp = DateUtils.getTimestamp("3/12/2018");
//        System.out.println(timestamp);
//        System.out.println(DateUtils.getDateFormat(timestamp));
        init();
    }

    public static void init(){
        // spark配置文件
        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("analysis");
        // spark sql上下文对象
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        Dataset<Row> load = spark.read().format("csv").option("header", "true").load("C:\\Users\\crescent\\Desktop\\q2.csv");
        Dataset<Row> drop = load.na().drop();
        drop.show();


        /*+------------+-------+---------+---------------+---------+---------+---+---------+---+-----+----+
          |         skc|year_id|season_id|tiny_class_code|tag_price| date_rcd|  s|real_cost| ie|month|year|
          +------------+-------+---------+---------------+---------+---------+---+---------+---+-----+----+*/
        JavaRDD<Row> rowJavaRDD = drop.toJavaRDD().filter(x->{
            if (StringUtils.isNotBlank(x.getString(0))
                    &&StringUtils.isNotBlank(x.getString(1))
                    &&StringUtils.isNotBlank(x.getString(2))
                    &&StringUtils.isNotBlank(x.getString(3))
                    &&StringUtils.isNotBlank(x.getString(4))
                    &&StringUtils.isNotBlank(x.getString(5))
                    &&StringUtils.isNotBlank(x.getString(6))
                    &&StringUtils.isNotBlank(x.getString(7))
                    &&StringUtils.isNotBlank(x.getString(8))
                    &&StringUtils.isNotBlank(x.getString(9))
                    &&StringUtils.isNotBlank(x.getString(10))) {
                return  true;
            } else {
                return  false;
            }
        });
        JavaPairRDD<Tuple3<String, String, String>, Iterable<Row>> groupedRdd = rowJavaRDD.groupBy(x -> new Tuple3<>(x.getString(3), x.getString(9), x.getString(10)));
        // 计算
        JavaRDD<Row> map = groupedRdd.map(x -> {
            Tuple3<String, String, String> key = x._1;
            List<Row> list = IteratorUtils.toList(x._2.iterator());
            System.out.println(key);
            List<Row> collect = list.stream().filter(n -> {
                if (StringUtils.isNotBlank(n.getString(0))
                        && StringUtils.isNotBlank(n.getString(1))
                        && StringUtils.isNotBlank(n.getString(2))
                        && StringUtils.isNotBlank(n.getString(3))
                        && StringUtils.isNotBlank(n.getString(4))
                        && StringUtils.isNotBlank(n.getString(5))
                        && StringUtils.isNotBlank(n.getString(6))
                        && StringUtils.isNotBlank(n.getString(7))
                        && StringUtils.isNotBlank(n.getString(8))
                        && StringUtils.isNotBlank(n.getString(9))
                        && StringUtils.isNotBlank(n.getString(10))) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
            collect.sort((x1, x2) -> {
                long timestamp = getTimestamp(x1.getString(5));
                long timestamp1 = getTimestamp(x2.getString(5));
                return Long.compare(timestamp, timestamp1);
            });
            double sum = collect.stream().mapToDouble(n -> Double.parseDouble(n.getString(6))).sum();
            System.out.println(sum);
            double sub = Integer.parseInt(collect.get(0).getString(6)) - Integer.parseInt(collect.get(collect.size() - 1).getString(6));
            System.out.println(collect.get(0));
            System.out.println(collect.get(collect.size() - 1));
            System.out.println(sub);
//            System.out.println();
            List<String> collect1 = collect.stream().map(n -> n.getString(0)).distinct().collect(Collectors.toList());
            System.out.println(collect1);
            int size = collect1.size();
            System.out.println(size);
            System.out.println();
            return RowFactory.create(key._1(), key._2(), key._3(), sum, sub, size);

        });
        map.collect();
        StructType schema = new StructType(new StructField[]{
                new StructField("tiny_class_code", DataTypes.StringType, true, Metadata.empty()),
                new StructField("month", DataTypes.StringType, true, Metadata.empty()),
                new StructField("year", DataTypes.StringType, true, Metadata.empty()),
                new StructField("sum_s", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("ie", DataTypes.DoubleType, true, Metadata.empty()),
                new StructField("skc_num", DataTypes.IntegerType, true, Metadata.empty()),
        });
        Dataset<Row> trackDf = spark.createDataFrame(map, schema);
        trackDf.show();
        trackDf.write().format("jdbc").mode(SaveMode.Overwrite)
                .option("url", "jdbc:mysql://localhost:3306/mathorcup?rewriteBatchedStatements=true")
                .option("dbtable", "q2_init")
                .option("batchsize",10000)
                .option("isolationLevel","NONE")
                .option("truncate","false")
                .option("user", "root").option("password", "root").save();

    }
//    private static SimpleDateFormat sf = null;

    public static long getTimestamp(String time){
        SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            System.out.println("---------------"+time);
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String getDateFormat(Long time){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sf.format(time);
    }
}


class DateUtils{

}