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
import java.util.Collections;
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
        data.significance = signifyOrientations(data.orientations, data.clockwiseClassifications, data.counterClockwiseClassifications);

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

    /**
     * O(n)
     */
    private static int[] signifyOrientations(OrientationSet orientations, int[] clockwise, int[] counterClockwise) {
        var size = clockwise.length;
        var array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = calculateSignificance(clockwise[i], counterClockwise[i], orientations.size());
        }

        return array;
    }

    private static int calculateSignificance(int clockwise, int counterClockwise, int orientationCount) {
        // 1 and 1 -> 1 <== Problematic?
        // 2 and 2 -> 2
        if (clockwise == counterClockwise)
            return clockwise;

        // 0 and 2 -> -1 No shared orientation
        if (isAligned(clockwise) && isAligned(counterClockwise))
            return -1;

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
        if ((min + orientationCount + orientationCount) - max == 2 && min == 1)
            return 0;

        // -1 and 0 -> 0
        if (orientationCount + orientationCount - max == 1 && min == 0)
            return 0;

        assert Math.abs(clockwise - counterClockwise) > 2;
        return -1;
    }

    private static int getSignificance(Data data, int index) {
        return data.significance[index];
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

        // Todo: calculate step-count somehow
        var steps = 5;

        var size = data.clockwiseClassifications.length;
        var prevIndex = (data.currentIndex + size - 1) % size;
        var index = data.currentIndex;
        var nextIndex = (index + 1) % size;
        var nextNextIndex = (index + 2) % size;

        var clockwise = data.clockwiseClassifications[index];
        var counterClockwise = data.counterClockwiseClassifications[nextIndex];

        var prev = polygon.vertex(prevIndex);
        var current = polygon.vertex(index);
        var next = polygon.vertex(nextIndex);
        var nextNext = polygon.vertex(nextNextIndex);

        var currentSignificance = getSignificance(data, index);
        var nextSignificance = getSignificance(data, nextIndex);

        if (isAligned(clockwise)) {
            line.addVertex(next.clone());
        } else {
            // Split edge for double significance, letting us handle it separately on both sides
            if (isSignificant(data, nextIndex)) {
                var inbetween = Vector.divide(Vector.add(current, next), 2);

                var points = buildStaircase(data.orientations, current, inbetween, prev, clockwise, currentSignificance, steps);
                points.remove(points.size() - 1);
                points.forEach(line::addVertex);

                var next_points = buildStaircase(data.orientations, next, inbetween, nextNext, counterClockwise, nextSignificance, steps);
                Collections.reverse(next_points);
                //next_points.remove(0);
                next_points.forEach(line::addVertex);

                line.addVertex(next.clone());
            } else {
                var points = buildStaircase(data.orientations, current, next, prev, clockwise, currentSignificance, steps);
                points.forEach(line::addVertex);
            }
        }

        data.currentIndex++;
    }

    public static List<Vector> buildStaircase(OrientationSet orientations, Vector start, Vector end, Vector prev, int classification, int significance, int steps) {
        var points = new ArrayList<Vector>();

        var direction = Vector.subtract(end, start);

        var stepSize = direction.length() / (steps * 2);
        var step = direction.clone();
        step.normalize();

        var before = orientations.get(classification / 2);
        var after = orientations.get((classification / 2 + 1) % orientations.size());

        OrientationSet.Orientation assigned = null;
        OrientationSet.Orientation associated = null;

        if (significance % 2 == 0) {
            assigned = before.getIndex() != significance / 2 ? before : after;
            associated = before.getIndex() != significance / 2 ? after : before;
        } else { // (significance % 2 == 1)
            var closest = getClosest(orientations, direction, Vector.subtract(prev, start));
            assigned = before == closest ? before : after;
            associated = before == closest ? after : before;
        }

        var ratios = Vector.solveVectorAddition(assigned.getDirection(), associated.getDirection(), step);
        var assignedStep = Vector.multiply(stepSize * ratios[0], assigned.getDirection());
        var associatedStep = Vector.multiply(stepSize * ratios[1], associated.getDirection());


        var now = start.clone();
        if (significance >= 0) {
            // Significance implies we have an evading edge, so split up steps to do evading ones first
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, assignedStep, associatedStep, true);
            }
            // Remove last point as last step isn't actually a half step
            points.remove(points.size() - 1);
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, associatedStep, assignedStep, true);
            }
        } else {
            for (int i = 0; i < steps; i++) {
                buildStep(points, now, assignedStep, associatedStep, false);
                buildStep(points, now, associatedStep, assignedStep, false);
            }
            points.add(now.clone());
        }


        return points;
    }

    public static OrientationSet.Orientation getClosest(OrientationSet orientations, Vector direction, Vector backDirection) {
        var orientation = Vector.up().computeClockwiseAngleTo(direction);
        var backOrientation = Vector.up().computeClockwiseAngleTo(backDirection);
        var closest = orientations.getClosest(orientation);
        var backClosest = orientations.getClosest(backOrientation);

        if (closest != backClosest)
            return closest;

        // Should be the same for both directions
        var before = orientations.getBefore(orientation);
        var after = orientations.getAfter(orientation);


        return before.getDistance(direction) < before.getDistance(backDirection) ? before : after;
    }

    public static void buildStep(List<Vector> points, Vector now, Vector a, Vector b, boolean halfStep) {
        now.translate(a);
        points.add(now.clone());
        now.translate(b);
        if (halfStep) points.add(now.clone());
    }

    //region debug drawing

    public static void resetDebug(Data data) {
        data.debugLines.clear();
        data.debugArrows.clear();
    }

    public static void drawDebug(Data data, DrawPanel panel) {
        if (data.original == null) return;

        if (data.drawOrientations)
            drawOrientations(panel, data.original, data.orientations);
        if (data.drawClassifications)
            drawClassification(panel, data.original, data.clockwiseClassifications, data.counterClockwiseClassifications, data.orientations);
        if (data.drawSignificance)
            drawSignificance(panel, data.original, data.significance, data.orientations);
    }

    private static void drawOrientations(DrawPanel panel, Polygon original, OrientationSet orientations) {
        panel.setStroke(Color.gray, 2, Dashing.SOLID);
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 3);

        for (int i = 0; i < original.vertexCount(); i++) {
            var v = original.vertex(i);

            orientations.forEach(orientation -> {
                var dir = orientation.getDirection();
                dir.scale(panel.convertViewToWorld(35));
                panel.draw(new LineSegment(v, Vector.add(v, dir)));
            });
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawClassification(DrawPanel panel, Polygon original, int[] clockwise, int[] counterClockwise, OrientationSet orientations) {
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 3);

        for (int i = 0; i < original.vertexCount(); i++) {
            var v = original.vertex(i);
            var clockwiseClassification = clockwise[i];
            var counterClockwiseClassification = counterClockwise[i];


            panel.setStroke(Color.yellow, 2, Dashing.SOLID);
            drawClassificationHelper(panel, orientations, v, clockwiseClassification);

            panel.setStroke(Color.green, 2, Dashing.SOLID);
            drawClassificationHelper(panel, orientations, v, counterClockwiseClassification);
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawSignificance(DrawPanel panel, Polygon original, int[] significances, OrientationSet orientations) {
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 3);
        panel.setStroke(Color.red, 2, Dashing.SOLID);

        for (int i = 0; i < original.vertexCount(); i++) {
            var v = original.vertex(i);
            var significance = significances[i];
            if (significance >= 0) {
                drawClassificationHelper(panel, orientations, v, significance);
            }
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawClassificationHelper(DrawPanel panel, OrientationSet orientations, Vector v, int classification) {
        var before = orientations.get(classification / 2).getDirection();
        before.scale(panel.convertViewToWorld(35));
        panel.draw(new LineSegment(v, Vector.add(v, before)));

        if (isUnaligned(classification)) {
            var after = orientations.get((classification / 2 + 1) % orientations.size()).getDirection();
            after.scale(panel.convertViewToWorld(35));
            panel.draw(new LineSegment(v, Vector.add(v, after)));
        }
    }

    //endregion debug drawing
}
