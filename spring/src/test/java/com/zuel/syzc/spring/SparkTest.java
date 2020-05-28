package com.zuel.syzc.spring;

import com.zuel.syzc.spark.SparkEntry;
import org.junit.Test;

public class SparkTest {
    @Test
    public void test(){
        SparkEntry sparkEntry = new SparkEntry();
        int entry = sparkEntry.entry(new String[]{"0"});
        System.out.println(entry);

    }
}
