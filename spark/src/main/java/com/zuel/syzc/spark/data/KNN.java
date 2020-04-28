package com.zuel.syzc.spark.data;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


/**
 * Created by lsy 983068303@qq.com
 * on 2016/12/15.
 */
public class KNN {
    public static void main(String[] args) throws Exception {
        SparkConf conf = new SparkConf();
        conf.setMaster("local[4]");
        conf.setAppName("knn");
//        conf.set("spark.executor.memory","1G");
//        conf.set("spark.storage.memoryFraction","1G");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SparkSession spark = SparkSession.builder().config(conf).getOrCreate();

        Dataset<Row> data = spark.sql("select * from raw_data");
        JavaRDD<Row> javaRdd = data.toJavaRDD();

//        List<Row<Integer>> data = new ArrayList<Row<Integer>>();
//        for (int i = 0; i < 100; i++) {
//            data.add(new Node(String.valueOf(i), i));
//        }

        final SimilarityInterface<Integer> similarity = new SimilarityInterface<Integer>() {
            public double similarity(Integer value1, Integer value2) {
                return 1.0 / (1.0 + Math.abs((Integer) value1 - (Integer) value2));
            }
        };

        NNDescent nndes = new NNDescent<Integer>();
        nndes.setK(30);
        nndes.setMaxIterations(4);
        nndes.setSimilarity(similarity);
        JavaPairRDD<Row, NeighborList> graph = nndes.computeGraph(nodes);

        graph.saveAsTextFile("out");
        ExhaustiveSearch exhaustive_search
                = new ExhaustiveSearch(graph, similarity);
        graph.cache();
        final Row<Integer> query = new Row(String.valueOf(111), 50);
        final NeighborList neighborlist_exhaustive
                = exhaustive_search.search(query, 5);
        for (Neighbor n : neighborlist_exhaustive) {
            System.out.print("id编号：" + n.node.id + "==============");
            System.out.println("对应的数值：" + n.node.id);
        }
        sc.stop();
    }
}