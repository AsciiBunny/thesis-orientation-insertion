package blankishproject;

import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Compass {

    public static final double SIZE = 100;
    public static final int GROUPS = 8;

    public static void draw(Data data, DrawPanel panel) {
        drawOrientationSet(panel, data.orientations, offset(panel, -25 - SIZE, -25 - SIZE));

        if (data.simplification == null) return;


        var counts = new int[GROUPS];
        data.simplification.edges().forEach(lineSegment -> {
            var dir = lineSegment.getDirection();
            var angle = dir.computeClockwiseAngleTo(Vector.up());
            var degrees = angle / (2 * Math.PI) * 360;
            var group = (int) Math.round(degrees / (GROUPS)) % GROUPS;
            counts[group]++;
        });

        //System.out.println("counts = " + Arrays.toString(counts));
        drawCounts(panel, counts, offset(panel, -25 - SIZE, -25 - SIZE * 4));
    }

    private static void drawOrientationSet(DrawPanel panel, OrientationSet orientations, Vector center) {
        panel.setStroke(Color.darkGray, 3, Dashing.SOLID);
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 2.5);

        orientations.forEach(orientation -> {
            var dir = orientation.getDirection();
            dir.scale(panel.convertViewToWorld(SIZE));
            panel.draw(new LineSegment(center, Vector.add(center, dir)));
        });

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawCounts(DrawPanel panel, int[] counts, Vector center) {
        var maxCount = Arrays.stream(counts).max().getAsInt();
        var lengthRatio = panel.convertViewToWorld(SIZE);
        var segmentSize = (Math.PI) / counts.length;

        for (int i = 0, countsLength = counts.length; i < countsLength; i++) {
            int count = counts[i];
            var length = count * 1.0 / maxCount;

            var dir = Vector.up();
            dir.scale(length * lengthRatio);
            dir.rotate(segmentSize * i);
            var dir2 = dir.clone();
            dir.rotate(segmentSize);

            dir.translate(center);
            dir2.translate(center);

            panel.draw(new LineSegment(center, dir));
            panel.draw(new LineSegment(center, dir2));
            panel.draw(new CircularArc(center, dir, dir2, false));

            dir.rotate(Math.PI, center);
            dir2.rotate(Math.PI, center);

            panel.draw(new LineSegment(center, dir));
            panel.draw(new LineSegment(center, dir2));
            panel.draw(new CircularArc(center, dir, dir2, false));

        }
    }

    private static Vector offset(DrawPanel panel, double x,  double y) {
        return panel.convertViewToWorld(new Vector(panel.getWidth() + x, panel.getHeight() + y));
    }
}
