package com.zuel.syzc.spark.kit;

import org.junit.Test;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JavaTest {
    @Test
    public void getCells() {
        List<BaseStationPoint> points = new ArrayList<>();
        points.add(new BaseStationPoint(0, 0));
        points.add(new BaseStationPoint(1, 0));
        points.add(new BaseStationPoint(2, 1));
        points.add(new BaseStationPoint(1, 2));
        points.add(new BaseStationPoint(0, 2));
        points.add(new BaseStationPoint(1, 1));
        GetCells getCells = new GetCells();
        List<Tuple2<String, Integer>> cellsInCircle = getCells.getCellsInCircle(123.4159698, 41.80778122, 1000);
        System.out.println("cellsInCircle = " + cellsInCircle);
        List<Tuple2<String, Integer>> cellsInPolygon = getCells.getCellsInPolygon(points);
        System.out.println("cellsInPolygon = " + cellsInPolygon);
        // Point point = new Point(1.6, 0.5);
        // System.out.println("getCells.isPointInPolygon(point, points) = " + getCells.isPointInPolygon(point, points));
    }

    @Test
    public void getBoundingPolygon() {
        BaseStationPoint[] baseStationPointsArray = {
                new BaseStationPoint(0, 0),
                new BaseStationPoint(1, 1),
                new BaseStationPoint(0, 1),
                new BaseStationPoint(2, 1),
                new BaseStationPoint(3, 1),
                new BaseStationPoint(2, 2),
                new BaseStationPoint(3, 2)
        };
        List<BaseStationPoint> baseStationPoints = Arrays.asList(baseStationPointsArray);
        LinkedList<BaseStationPoint> bounds = MinimumBoundingPolygon.findSmallestPolygon(baseStationPoints);
        bounds.forEach(x -> System.out.println(x.toString()));
    }

}
