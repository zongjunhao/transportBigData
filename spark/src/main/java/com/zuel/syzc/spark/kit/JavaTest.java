package com.zuel.syzc.spark.kit;

import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class JavaTest {
    public static void main(String[] args) {
        List<Point> points = new ArrayList<>();
        points.add(new Point(0, 0));
        points.add(new Point(1, 0));
        points.add(new Point(2, 1));
        points.add(new Point(1, 2));
        points.add(new Point(0, 2));
        points.add(new Point(1, 1));
        GetCells getCells = new GetCells();
        List<Tuple2<String, Integer>> cellsInCircle = getCells.getCellsInCircle(123.4159698, 41.80778122, 1000);
        System.out.println("cellsInCircle = " + cellsInCircle);
        List<Tuple2<String, Integer>> cellsInPolygon = getCells.getCellsInPolygon(points);
        System.out.println("cellsInPolygon = " + cellsInPolygon);
        // Point point = new Point(1.6, 0.5);
        // System.out.println("getCells.isPointInPolygon(point, points) = " + getCells.isPointInPolygon(point, points));
    }
}
