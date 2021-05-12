package blankishproject;

import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Schematization {

    public static long totalTimeTaken = 0;
    public static long lastCycleTimeTaken = 0;

    public static void run(Data data) {
        timedIteration(data);
    }

    public static void finish(Data data) {
        while (data.currentIndex < data.original.vertexCount()) {
            timedIteration(data);
        }
    }

    public static void timedIteration(Data data) {
        long start = System.currentTimeMillis();
        iteration(data);
        long end = System.currentTimeMillis();
        long duration = end - start;
        totalTimeTaken += duration;
        lastCycleTimeTaken = duration;
    }

    public static void init(Data data) {
        data.schematization = new PolyLine();
        data.clockwiseClassifications = classifyOrientations(data.original, data.orientations, false);
        data.counterClockwiseClassifications = classifyOrientations(data.original, data.orientations, true);
        data.currentIndex = 0;

        data.schematization.addVertex(data.original.vertex(0));
    }

    /**
     * O(n)
     */
    private static int[] classifyOrientations(Polygon polygon, OrientationSet orientations, boolean counterClockwise) {
        var size = polygon.vertexCount();
        var classifications = new int[size];

        for (int i = 0; i < size; i++) {
            var current = counterClockwise ? polygon.vertex(((i - 1) + size) % size) : polygon.vertex(i);
            var next = counterClockwise ? polygon.vertex(i) : polygon.vertex((i + 1) % size);

            var direction = counterClockwise ? Vector.subtract(current, next) : Vector.subtract(next, current);
            var orientation = Vector.up().computeClockwiseAngleTo(direction);

            var before = orientations.getBefore(orientation);
            var after = orientations.getAfter(orientation);

            if (before == after) {
                classifications[i] = before.getIndex() * 2;
            } else {
                classifications[i] = before.getIndex() * 2 + 1;
            }
        }

        return classifications;
    }

    private static int[] signifyOrientations(int[] clockwise, int[] counterClockwise) {
        var size = clockwise.length;
        var array = new int[size];

        for (int i = 0; i < size; i++) {

        }

        return array;
    }

    private static int getSignificance(int clockwise, int counterClockwise, int count) {
        if (isAligned(clockwise) && isAligned(counterClockwise))
            return -1;

        if (clockwise == counterClockwise)
            return clockwise;

        // 1 and 3 -> 2
        if (Math.abs(clockwise - counterClockwise) == 2)
            return (clockwise + counterClockwise) / 2;

        // 1 and 2 -> 2
        // 2 and 3 -> 2
        if (Math.abs(clockwise - counterClockwise) == 1)
            return clockwise % 2 == 0 ? clockwise : counterClockwise;

        // last-first index adjacency:
        var max = Math.max(clockwise, counterClockwise);
        var min = Math.min(clockwise, counterClockwise);

        // -1 and 1 -> 0
        if ((min + count) - max == 2)
            return 0;

        // -1 and 0 -> 0
        if ((min + count) - max == 1)
            return 0;

        assert Math.abs(clockwise - counterClockwise) > 2;
        return -1;
    }

    private static int getSignificance(Data data, int index) {
        return getSignificance(data.clockwiseClassifications[index], data.counterClockwiseClassifications[index], data.original.vertexCount());
    }

    private static boolean isSignificant(int clockwise, int counterClockwise, int count) {
        return getSignificance(clockwise, counterClockwise, count) >= 0;
    }

    private static boolean isSignificant(Data data, int index) {
        return getSignificance(data, index) >= 0;
    }

    private static boolean isAligned(int classification) {
        return classification % 2 == 0;
    }

    private static boolean isUnaligned(int classification) {
        return classification % 2 == 1;
    }

    public static void iteration(Data data) {
        if (data.currentIndex >= data.original.vertexCount())
            return;

        var polygon = data.original;
        var line = data.schematization;

        var size = data.clockwiseClassifications.length;
        var index = data.currentIndex;
        var nextIndex = (index + 1) % size;

        var clockwise = data.clockwiseClassifications[index];
        var counterClockwise = data.counterClockwiseClassifications[index];

        var current = polygon.vertex(index);
        var next = polygon.vertex(nextIndex);

        // Todo: Cache significance
        var currentSignificance = getSignificance(data, index);
        var nextSignificance = getSignificance(data, nextIndex);

        if (isAligned(clockwise)) {
            line.addVertex(next.clone());
        } else {
            // TODO: split edge for double significance

            // TODO: Pass along whether evading
            // evading = significance in start

            var points = buildStaircase(data.orientations, current, next, clockwise, 5, currentSignificance);
            points.forEach(line::addVertex);
        }

        data.currentIndex++;
    }

    public static List<Vector> buildStaircase(OrientationSet orientations, Vector start, Vector end, int classification, int steps, int significance) {
        var points = new ArrayList<Vector>();

        var direction = Vector.subtract(end, start);
        var orientation = Vector.up().computeClockwiseAngleTo(direction);
        var closest = orientations.getClosest(orientation);
        // Todo: calculate step-size somehow
        var stepSize = direction.length() / (steps * 2);
        var step = direction.clone();
        step.normalize();
        step.scale(stepSize);

        var before = orientations.get(classification / 2);
        var after = orientations.get((classification / 2 + 1) % orientations.size());

        OrientationSet.Orientation assigned = null;
        OrientationSet.Orientation associated = null;

        if (significance % 2 == 0) {
            assigned = before.getIndex() != significance / 2 ? before : after;
            associated = before.getIndex() != significance / 2 ? after : before;
        } else {
            assigned = before == closest ? before : after;
            associated = before == closest ? after : before;
        }


        var assignedStepRatio = Vector.dotProduct(assigned.getDirection(), step) / step.length();
        var assignedStep = Vector.multiply(stepSize * assignedStepRatio, assigned.getDirection());

        var associatedStepRatio = Vector.dotProduct(associated.getDirection(), step)  / step.length();
        var associatedStep = Vector.multiply(stepSize * associatedStepRatio, associated.getDirection());


        var now = start.clone();
        if (significance >= 0) {
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, assignedStep, associatedStep);
            }
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, associatedStep, assignedStep);
            }
        } else {
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, assignedStep, associatedStep);
                buildStep(points, now, associatedStep, assignedStep);
            }
        }


        return points;
    }

    public static void buildStep(List<Vector> points, Vector now, Vector a, Vector b) {
        now.translate(a);
        points.add(now.clone());
        now.translate(b);
        points.add(now.clone());
    }

    //region debug drawing

    public static void resetDebug(Data data) {
        data.debugLines.clear();
        data.debugArrows.clear();
    }

    public static void drawDebug(Data data, DrawPanel panel) {
        if (data.original == null) return;

        drawOrientations(panel, data.original, data.orientations, Color.gray);
        drawClassification(panel, data.original, data.clockwiseClassifications, data.counterClockwiseClassifications, data.orientations, Color.GREEN);
    }

    private static void drawOrientations(DrawPanel panel, Polygon original, OrientationSet orientations, Color color) {
        panel.setStroke(color, 1.5, Dashing.SOLID);
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 2.5);

        for (int i = 0; i < original.vertexCount(); i++) {
            var v = original.vertex(i);

            orientations.forEach(orientation -> {
                var dir = orientation.getDirection();
                dir.scale(10);
                panel.draw(new LineSegment(v, Vector.add(v, dir)));
            });
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawClassification(DrawPanel panel, Polygon original, int[] clockwise, int[] counterClockwise, OrientationSet orientations, Color color) {
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 2.5);

        for (int i = 0; i < original.vertexCount(); i++) {
            var v = original.vertex(i);
            var clockwiseClassification = clockwise[i];
            var counterClockwiseClassification = counterClockwise[i];


            panel.setStroke(Color.yellow, 1.5, Dashing.SOLID);
            drawClassificationHelper(panel, orientations, v, clockwiseClassification);

            panel.setStroke(Color.green, 1.5, Dashing.SOLID);
            drawClassificationHelper(panel, orientations, v, counterClockwiseClassification);

            var significance = getSignificance(clockwiseClassification, counterClockwiseClassification, clockwise.length);
            if (significance >= 0) {

                panel.setStroke(Color.red, 1.5, Dashing.SOLID);
                drawClassificationHelper(panel, orientations, v, significance);
            }
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawClassificationHelper(DrawPanel panel, OrientationSet orientations, Vector v, int classification) {
        var before = orientations.get(classification / 2).getDirection();
        before.scale(10);
        panel.draw(new LineSegment(v, Vector.add(v, before)));

        if (isUnaligned(classification)) {
            var after = orientations.get((classification / 2 + 1) % orientations.size()).getDirection();
            after.scale(10);
            panel.draw(new LineSegment(v, Vector.add(v, after)));
        }
    }

    //endregion debug drawing
}
