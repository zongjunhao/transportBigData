package com.zuel.syzc.spark.crowd;

import scala.Serializable;
import scala.Tuple2;

import java.util.Comparator;

public class TimeComparator implements Comparator<Tuple2<Long, Integer>>, Serializable {

    @Override
    public int compare(Tuple2<Long, Integer> o1, Tuple2<Long, Integer> o2) {
        return o1._1>o2._1?1:-1;
    }
}