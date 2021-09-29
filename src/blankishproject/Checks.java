package blankishproject;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;

import java.util.ArrayList;

public class Checks {

    // Algorithms assume vertices are given clockwise, make sure this is the case
    public static void correctOrder(Data data) {
        var polygon = data.original;

        var area = polygon.areaSigned();

        if (area < 0) {
            System.out.println("Input was counter-clockwise; Reversing...");
            polygon.reverse();
        }
    }

    public static void uniqueVertices(Data data) {
        var polygon = data.original;
        var vertices = polygon.vertices();
        var toRemove = new ArrayList<Vector>();

        for (int i = 0; i < vertices.size(); i++) {
            var a = vertices.get(i);
            var ib = (i + 1) % vertices.size();
            var b = vertices.get(ib);

            if (a != b && a.isApproximately(b)) {
                // a and b overlap but are different Vector instances
                System.out.println(i + ":" + a + " & " + ib + ":" + b + " are too similar, removing the latter");
                toRemove.add(b);
            }
        }

        vertices.removeAll(toRemove);
        System.out.println("Removed " + toRemove.size() + " duplicate vertices");
    }

    public static void noUnnecessaryVertices(Data data) {
        var polygon = data.original;
        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edges::add);
        var toRemove = new ArrayList<Vector>();

        for (int i = 0; i < edges.size(); i++) {
            var a = edges.get(i);
            var ib = (i + 1) % edges.size();
            var b = edges.get(ib);

            var dirA = a.getDirection();
            var dirB = b.getDirection();

            if (a != b && dirA.isApproximately(dirB)) {
                // a and b could be one edge but are different Vector instances
                System.out.println(i + ":" + dirA + " & " + ib + ":" + dirB + " directions are too similar, removing the middle vector");
                toRemove.add(b.getStart());
            }
        }

        polygon.vertices().removeAll(toRemove);
        System.out.println("Removed " + toRemove.size() + " unnecessary vertices");
    }

    public static void no360DegreeTurns(Data data) {
        var polygon = data.original;
        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edges::add);
        var toRemove = new ArrayList<Vector>();

        for (int i = 0; i < edges.size(); i++) {
            var a = edges.get(i);
            var ib = (i + 1) % edges.size();
            var b = edges.get(ib);

            var dirA = a.getDirection();
            var dirB = b.getDirection()
                    .clone();
            dirB.scale(-1);

            if (a != b && dirA.isApproximately(dirB)) {
                // a and b could be one edge but are different Vector instances
                System.out.println(i + ":" + a + " & " + ib + ":" + b + " directions are exact opposites, removing the middle vector");
                toRemove.add(b.getStart());
            }
        }

        polygon.vertices().removeAll(toRemove);
        System.out.println("Removed " + toRemove.size() + " 360 degree turns");
    }
}
