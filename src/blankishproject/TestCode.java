package blankishproject;

import blankishproject.simplification.Simplification;
import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TestCode {

    public static void run(Data data) {
        var polygon = data.original;
        var areaBefore = polygon.areaUnsigned();
        rotateEdgeAroundMiddle(polygon, 6, getDirectionFromDegrees(135));
        System.out.println(areaBefore + " -> " + polygon.areaUnsigned());

        Simplification.initState(data.simplificationData, data.original, data.dialog);
    }

    public static void rotateEdgeAroundMiddle(Polygon polygon, int index, Vector orientation) {
        var a = polygon.vertex(index);
        var b = polygon.vertex(index + 1);
        var c = polygon.vertex(index + 2);
        var d = polygon.vertex(index + 3);

        var prevSegment = new LineSegment(a, b).clone();
        var nextSegment = new LineSegment(c, d).clone();

        var middle = Vector.add(b, c);
        middle.scale(0.5);
        var newLine = new Line(middle, orientation);

        var prevIntersections = newLine.intersect(prevSegment);
        if (!(prevIntersections.size() == 1 && prevIntersections.get(0) instanceof Vector ) ) {
            System.out.println("Invalid rotation: no prev collision");
            return;
        }
        var nextIntersections = newLine.intersect(nextSegment);
        if (!(nextIntersections.size() == 1 && nextIntersections.get(0) instanceof Vector ) ) {
            System.out.println("Invalid rotation: no next collision");
            return;
        }

        var newB = (Vector) prevIntersections.get(0);
        var newC = (Vector) nextIntersections.get(0);

        b.set(newB);
        c.set(newC);
    }

    public static void rotateEdgeAroundFirst(Polygon polygon, int index, Vector orientation) {
        var b = polygon.vertex(index + 1);
        var c = polygon.vertex(index + 2);
        var d = polygon.vertex(index + 3);

        var nextSegment = new LineSegment(c, d);
        var newLine = new Line(b, orientation);
        var intersections = newLine.intersect(nextSegment);
        if (intersections.size() == 1) {
            var newC = (Vector) intersections.get(0);
            c.set(newC);

            if (polygon.vertex(index + 2).isApproximately(polygon.vertex(index + 3))) {
                polygon.removeVertex(index + 3);
            }
        } else {
            System.out.println("Invalid rotation");
        }
    }

    public static void draw(Data data, DrawPanel panel) {
        if (data.original != null && data.original.vertexCount() > 0) {
            var b = data.original.vertex(7);
            //panel.draw(new Line(b, getDirectionFromDegrees(135)));
        }
    }

    public static Vector getDirectionFromDegrees(double degrees) {
        var radial = degrees / 360 * 2 * Math.PI;
        var direction = Vector.up();
        direction.rotate(-radial);
        return direction;
    }
}
