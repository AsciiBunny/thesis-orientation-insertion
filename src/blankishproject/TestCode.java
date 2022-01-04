package blankishproject;

import blankishproject.simplification.Simplification;
import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TestCode {

    public static void run(Data data) {
//        var areaBefore = polygon.areaUnsigned();
//        rotateEdgeAroundMiddle(polygon, 2);
//        System.out.println(areaBefore + " -> " + polygon.areaUnsigned());
//
//        // Use as input
//        data.original = polygon.clone();
//
//        Simplification.initState(data.simplificationData, data.original);
//        Schematization.init(data);

    }

    public static void rotateEdgeAroundMiddle(Polygon polygon, int index) {
        var a = polygon.vertex(index);
        var b = polygon.vertex(index + 1);
        var c = polygon.vertex(index + 2);
        var d = polygon.vertex(index + 3);

        var prev = new LineSegment(a, b).clone();
        var inner = new LineSegment(b, c).clone();
        var next = new LineSegment(c, d).clone();

        var prevLine = Util.extendLine(prev);
        var nextLine = Util.extendLine(next);

        var m = Vector.add(b, c);
        m.scale(0.5);
        inner.rotate(1, m);

        var innerLine = Util.extendLine(inner);
        var newB = (Vector) innerLine.intersect(prevLine).get(0);
        var newC = (Vector) innerLine.intersect(nextLine).get(0);

        b.set(newB);
        c.set(newC);
    }

    public static void rotateEdgeAroundFirst(Polygon polygon, int index) {
        var b = polygon.vertex(index + 1);
        var c = polygon.vertex(index + 2);
        var d = polygon.vertex(index + 3);

        var inner = new LineSegment(b, c).clone();
        var next = new LineSegment(c, d).clone();

        var nextLine = Util.extendLine(next);

        inner.rotate(1, b);

        var innerLine = Util.extendLine(inner);
        var newC = (Vector) innerLine.intersect(nextLine).get(0);

        c.set(newC);
    }





//    public static void trapezoidTest(Data data) {
//        var polygon = ((Polygon) data.geometries.get(0));
//
//        //assert Util.extendLine(polygon.edge(0)).intersect(Util.extendLine(polygon.edge(2))).size() == 0;
//
//        var inner = polygon.edge(0);
//        var outer = polygon.edge(2);
//        var innerLength = inner.length();
//        var outerLength = outer.length();
//
//        var shorter = innerLength < outerLength ? inner : outer;
//        var longer = innerLength < outerLength ? outer : inner;
//        var shorterLength = shorter.length();
//        var longerLength = longer.length();
//
//
//        var totalArea = polygon.areaUnsigned();
//        var removeArea = totalArea / 4;
//
//        if (innerLength < outerLength)
//            removeArea = totalArea - removeArea;
//
//        var totalHeight = Util.extendLine(longer).closestPoint(shorter.getStart()).distanceTo(shorter.getStart());
//        var removeDirection = Vector.subtract(shorter.getStart(), Util.extendLine(longer).closestPoint(shorter.getStart()));
//        removeDirection.normalize();
//
//        var calcArea = 0.5 * totalHeight * (shorterLength + longerLength);
//        assert DoubleUtil.close(calcArea, totalArea) : calcArea + " != " + totalArea;
//
//        var midLength = Math.sqrt(2 * shorterLength * removeArea + longerLength * (longerLength * totalHeight - 2 * removeArea)) / Math.sqrt(totalHeight);
//        var removeDistance = (2 * removeArea) / (midLength + longerLength);
//
//        assert shorterLength < midLength || DoubleUtil.close(shorterLength, midLength);
//        assert midLength < longerLength || DoubleUtil.close(midLength, longerLength);
//        assert removeDistance > 0;
//        assert removeDistance < totalHeight;
//
//        System.out.println("midLength = " + midLength);
//        System.out.println("removeDistance = " + removeDistance);
//
//        var removeVector = Vector.multiply(removeDistance, removeDirection);
//        var mid = longer.clone();
//        mid.translate(removeVector);
//        var midLine = Util.extendLine(mid);
//
//        data.geometries.add(midLine);
//
//        mid.setStart((Vector) polygon.edge(1).intersect(midLine).get(0));
//        mid.setEnd((Vector) polygon.edge(3).intersect(midLine).get(0));
//
//        assert DoubleUtil.close(mid.length(), midLength);
//
//        var upper = new Polygon(inner.getStart().clone(), inner.getEnd().clone(), mid.getStart().clone(), mid.getEnd().clone());
//        var lower = new Polygon(mid.getStart().clone(), mid.getEnd().clone(), outer.getEnd().clone(), outer.getStart().clone());
//
//        upper.translate(longerLength * 1.5, 0);
//        lower.translate(longerLength * 1.5, -10);
//
//        var upperArea = upper.areaUnsigned();
//        var lowerArea = lower.areaUnsigned();
//
//        assert DoubleUtil.close(upperArea, removeArea) || DoubleUtil.close(lowerArea, removeArea);
//
//        data.geometries.add(upper);
//        data.geometries.add(lower);
//    }

    public static void draw(Data data, DrawPanel panel) {

    }

    public static void drawDot(Data data, DrawPanel panel) {
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);

        var origin = new Vector(0.0, 0.0);

        var distance = 222.0;
        var sharedDirection = new Vector(700.0, 500.0);
        sharedDirection.normalize();
        var shared = LineSegment.byStartDirectionAndLength(origin, sharedDirection, distance * 2.5);
        var part = LineSegment.byStartDirectionAndLength(origin, sharedDirection, distance);


        var normalVector = new Vector(-50.0, 300.0);
        normalVector.normalize();
        // Ratio = dot product of normalized directions of normal and shared
        var ratio = Vector.dotProduct(normalVector, sharedDirection);
        var normal = LineSegment.byStartDirectionAndLength(origin, normalVector.clone(), distance * ratio);

        //assert shared.closestPoint(normal.getEnd()).isApproximately(part.getEnd());
        assert normal.getEnd().isApproximately(Util.extendLine(normal).closestPoint(part.getEnd()));

        panel.setStroke(Color.BLUE, 3, Dashing.SOLID);
        panel.draw(shared);
        panel.setStroke(Color.RED, 3, Dashing.dashed(3));
        panel.draw(part);
        panel.setStroke(Color.GREEN, 3, Dashing.SOLID);
        panel.draw(normal);

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
        panel.setStroke(Color.black, 1, Dashing.SOLID);
        panel.draw(new LineSegment(part.getEnd(), Util.extendLine(normal).closestPoint(part.getEnd())));
    }
}
