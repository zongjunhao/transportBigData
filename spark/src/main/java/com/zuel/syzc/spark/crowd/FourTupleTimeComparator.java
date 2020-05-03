package com.zuel.syzc.spark.crowd;

import scala.Serializable;
import scala.Tuple3;
import scala.Tuple4;

import java.util.Comparator;

public class FourTupleTimeComparator implements Comparator<Tuple4<Long, String, String, String>>, Serializable {

    @Override
    public int compare(Tuple4<Long, String, String, String> o1, Tuple4<Long, String, String, String> o2) {
        return o1._1().compareTo(o2._1());
    }
}
