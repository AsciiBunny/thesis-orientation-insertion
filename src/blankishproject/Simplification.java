package blankishproject;

import blankishproject.deciders.IDecider;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

import java.awt.*;
import java.util.ArrayList;

public class Simplification {

    public static int totalVerticesRemoved = 0;
    public static int lastCycleVerticesRemoved = 0;

    public static double totalAreaEffected = 0;
    public static double lastCycleAreaEffected = 0;

    public static int totalMovesMade = 0;
    public static int lastCycleMovesMade = 0;

    public static long totalTimeTaken = 0;
    public static long lastCycleTimeTaken = 0;

    public static void reset() {
        totalVerticesRemoved = lastCycleVerticesRemoved = 0;
        totalAreaEffected = lastCycleAreaEffected = 0;
        totalMovesMade = lastCycleMovesMade = 0;
        totalTimeTaken = lastCycleTimeTaken = 0;
    }

    public static void run(Data data) {
        iteration(data, data.simplification);
    }

    public static void finish(Data data) {
        var polygon = data.simplification;

        var before = polygon.vertexCount();
        while(true) {
            var done = !iteration(data, polygon);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices, continuing");
            else
                System.out.println("Removed 0 vertices, stopping");

            before = after;

            if (done) break;
        }
    }

    public static boolean timedIteration(Data data, Polygon polygon) {
        long start = System.currentTimeMillis();
        var madeChanged = iteration(data, polygon);
        long end = System.currentTimeMillis();
        long duration = end - start;
        totalTimeTaken += duration;
        lastCycleTimeTaken = duration;
        return madeChanged;
    }

    public static boolean iteration(Data data, Polygon polygon) {
        var configurationList = new ConfigurationList(polygon);

        var debugLines = new GeometryList<LineSegment>();
        var debugArrows = new GeometryList<LineSegment>();


        // Determine minimal configuration pair
        //      Find smallest contraction possible O(n)
        //      Pair with the smallest non-conflicting contraction
        //
        var moves = new ArrayList<>(IDecider.deciders.get(data.deciderType).findMoves(polygon, configurationList));
        var madeChanges = moves.size() > 0;

        totalMovesMade += moves.size();
        lastCycleMovesMade = moves.size();
        lastCycleAreaEffected = 0;
        lastCycleVerticesRemoved = 0;

        while (moves.size() > 0) {
            var min = moves.remove(0);
            var configuration = min.configuration;

            totalAreaEffected += min.removeArea;
            lastCycleAreaEffected += min.removeArea;

            debugArrows.add(configuration.inner.clone());
            var removed = configuration.performMove(min.type, min.removeArea, min.requiresCleanup);

            totalVerticesRemoved += removed.size();
            lastCycleVerticesRemoved += removed.size();

            moves.forEach(c -> {
                removed.forEach(i -> {
                    if (i < c.configuration.index) {
                        c.configuration.index--;
                    }
                });
            });
            debugLines.add(configuration.inner.clone());
        }

        resetDebug(data);
        data.debugLines.put(Color.blue, debugLines);
        data.debugArrows.put(Color.blue, debugArrows);

        return madeChanges;
    }

    //region debug

    public static void resetDebug(Data data) {
        data.debugLines.clear();
        data.debugArrows.clear();
    }

    public static void drawDebug(Data data, DrawPanel panel) {
        if (data.simplification == null) return;
        var polygon = data.simplification;
        var configurationList = new ConfigurationList(polygon);

        if (data.drawConvexityArcs)
            drawDebugConvexityArcs(panel, configurationList, Color.red, Color.green);
        if (data.drawConvexityEdges)
            drawDebugConvexityLines(panel, configurationList, Color.red, Color.green);

        if (data.drawPositiveContractions)
            drawDebugPositiveContractions(panel, configurationList, Color.red, Color.green);
        if (data.drawNegativeContractions)
            drawDebugNegativeContractions(panel, configurationList, Color.red, Color.green);

        if (data.drawBlockingPoints)
            drawBlockingVectors(panel, configurationList, Color.cyan);
    }

    private static void drawBlockingVectors(DrawPanel panel, ConfigurationList list, Color color) {
        panel.setStroke(color, 3, Dashing.dashed(3));
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);

        for (var conf : list) {
            if (conf.positiveNormalMove.hasContraction()) {
                var blockers = conf.positiveNormalMove.getBlockingVectors();

                blockers.forEach(vector -> panel.draw(new LineSegment(conf.inner.getPointAlongPerimeter(0.5), vector)));
            }
            if (conf.negativeNormalMove.hasContraction()) {
                var blockers = conf.negativeNormalMove.getBlockingVectors();

                blockers.forEach(vector -> panel.draw(new LineSegment(conf.inner.getPointAlongPerimeter(0.5), vector)));
            }
        }

        panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
    }

    private static void drawMovementDirectionDebugLines(Data data, ConfigurationList list, Color outwardsColor, Color inwardsColor) {
        var outwardsDebugLines = data.debugLines.getOrDefault(outwardsColor, new GeometryList<>());
        var inwardsDebugLines = data.debugLines.getOrDefault(inwardsColor, new GeometryList<>());

        for (var conf : list) {
            var normal = conf.getNormal();

            // Draw copy of line moved in outwards direction
            var outwardsLine = conf.inner.clone();
            normal.scale(-50);
            outwardsLine.translate(normal);
            outwardsDebugLines.add(outwardsLine);

            // Draw copy of line moved in inwards direction
            var inwardsLine = conf.inner.clone();
            normal.scale(-1);
            inwardsLine.translate(normal);
            inwardsDebugLines.add(inwardsLine);

        }

        data.debugLines.put(outwardsColor, outwardsDebugLines);
        data.debugLines.put(inwardsColor, inwardsDebugLines);
    }

    private static void drawDebugPositiveContractions(DrawPanel panel, ConfigurationList list, Color contractionColor, Color directionColor) {
        for (var configuration : list) {
            if (!configuration.positiveNormalMove.hasValidContraction()) continue;
            var contraction = configuration.positiveNormalMove.getContraction();
            if (contraction != null) {
                panel.setStroke(contractionColor, 3, Dashing.SOLID);
                panel.draw(contraction);

                var a = configuration.inner.getPointAlongPerimeter(0.5);
                panel.setStroke(directionColor, 3, Dashing.dashed(3));
                panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)));
                panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);

//                panel.setStroke(directionColor, 3, Dashing.dashed(3));
//                panel.draw(configuration.inner);
//
//                panel.setStroke(extensionColor, 3, Dashing.dashed(3));
//                panel.draw(new LineSegment(contraction.getEnd(), configuration.next.getEnd()));
//                panel.draw(new LineSegment(contraction.getStart(), configuration.previous.getStart()));

                var area = configuration.positiveNormalMove.getArea();
                panel.setTextStyle(TextAnchor.BASELINE_CENTER, 16);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)).getPointAlongPerimeter(0.5), String.format("%.2f", area));

            }
        }
    }

    private static void drawDebugNegativeContractions(DrawPanel panel, ConfigurationList list, Color contractionColor, Color directionColor) {
        for (var configuration : list) {
            if (!configuration.negativeNormalMove.hasValidContraction()) continue;
            var contraction = configuration.negativeNormalMove.getContraction();
            if (contraction != null) {
                panel.setStroke(contractionColor, 3, Dashing.SOLID);
                panel.draw(contraction);

                var a = configuration.inner.getPointAlongPerimeter(0.5);
                panel.setStroke(directionColor, 3, Dashing.dashed(3));
                panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)));
                panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);

                var area = configuration.negativeNormalMove.getArea();
                panel.setTextStyle(TextAnchor.BASELINE_CENTER, 16);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)).getPointAlongPerimeter(0.5), String.format("%.2f", area));


            }
        }
    }

    private static void drawDebugConvexityArcs(DrawPanel panel, ConfigurationList list, Color convexColor, Color reflexColor) {
        for (var conf : list) {
            var arcStart = conf.previous.getDirection();
            arcStart.scale(-10);
            arcStart.translate(conf.previous.getEnd());

            var arcEnd = conf.inner.getDirection();
            arcEnd.scale(10);
            arcEnd.translate(conf.inner.getStart());

            var arc = new CircularArc(conf.inner.getStart(), arcStart, arcEnd, true);

            if (conf.isStartConvex()) {
                panel.setStroke(convexColor, 2, Dashing.SOLID);
                panel.draw(arc);
            } else {
                panel.setStroke(reflexColor, 2, Dashing.SOLID);
                panel.draw(arc);
            }
        }

    }

    private static void drawDebugConvexityLines(DrawPanel panel, ConfigurationList list, Color convexColor, Color reflexColor) {
        for (var conf : list) {
            if (conf.isInnerConvex()) {
                panel.setStroke(convexColor, 3, Dashing.SOLID);
                panel.draw(conf.inner);
            } else if (conf.isInnerReflex()) {
                panel.setStroke(reflexColor, 3, Dashing.SOLID);
                panel.draw(conf.inner);
            }
        }
    }

    //endregion debug
}
