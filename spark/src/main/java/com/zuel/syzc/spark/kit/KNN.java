package com.zuel.syzc.spark.kit;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KNN {
    private SparkSession spark;
    private Dataset<Row> eachUserBeforeKnn;

    public KNN(SparkSession spark, Dataset<Row> dataset) {
        this.spark = spark;
        eachUserBeforeKnn = dataset;
    }

    public Dataset<Row> processWithKnn(int i, int k) {
        List<Row> rowList = eachUserBeforeKnn.collectAsList();
        int n = rowList.size();
        for (int j = i; j != n - 2; j++) {
            Map<String, Integer> clusterCounter = new HashMap<>();
            for (int a = j - k / 2; a < j + k / 2 + 1; a++) {
                if (a != j) {
                    String cId = rowList.get(a - 1).get(6).toString();
                    if (clusterCounter.containsKey(cId)) {// 更新值
                        clusterCounter.put(cId, clusterCounter.get(cId) + 1);
                    } else {
                        clusterCounter.put(cId, 1);
                    }
                }
            }

            for(Map.Entry<String, Integer> entry : clusterCounter.entrySet()){
                String mapKey = entry.getKey();
                int mapValue = entry.getValue();
                if (mapValue > k / 2) {
                    rowList.get(j - 1).get(6);
//                    eachUserBeforeKnn
                    break;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {

    }
}
