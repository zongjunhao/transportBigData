package com.zuel.syzc.spark.crowd;

import scala.Serializable;
import scala.Tuple3;
import scala.Tuple4;

import java.util.Comparator;

public class ThreeTupleTimeComparator implements Comparator<Tuple3<Long, String, String>>, Serializable {
    @Override
    public int compare(Tuple3<Long, String, String> o1, Tuple3<Long, String, String> o2) {
        return o1._1().compareTo(o2._1());
    }
}
