package blankishproject.simplification;

import blankishproject.Data;
import blankishproject.GeometryList;
import blankishproject.Util;
import blankishproject.simplification.deciders.Decision;
import blankishproject.simplification.deciders.IDecider;
import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;
import nl.tue.geometrycore.util.DoubleUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    public static void initState(SimplificationData data, Polygon polygon) {
        data.init(polygon.clone());
    }

    public static void run(Data data) {
        timedIteration(data.simplificationData);

        data.innerDifference = Util.calculateSymmetricDifference(data.simplificationData.polygon, data.original);
        data.outerDifference = Util.calculateSymmetricDifference(data.original, data.simplificationData.polygon);
    }

    public static void run(Data data, int cycles) {
        var polygon = data.simplificationData.polygon;
        var before = polygon.vertexCount();
        for (int cycle = 0; cycle < cycles; cycle++) {
            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            before = after;

            if (done) break;
        }

        data.innerDifference = Util.calculateSymmetricDifference(polygon, data.original);
        data.outerDifference = Util.calculateSymmetricDifference(data.original, polygon);
    }

    public static void runUntilLeft(Data data, int left) {
        var polygon = data.simplificationData.polygon;

        var before = polygon.vertexCount();
        while (before > left) {
            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            before = after;

            if (done) break;
        }

        data.innerDifference = Util.calculateSymmetricDifference(polygon, data.original);
        data.outerDifference = Util.calculateSymmetricDifference(data.original, polygon);
    }

    public static void finish(Data data) {
        runUntilLeft(data, 0);
    }

    public static boolean timedIteration(SimplificationData data) {
        long start = System.currentTimeMillis();
        var madeChanged = iteration(data);
        long end = System.currentTimeMillis();
        long duration = end - start;
        totalTimeTaken += duration;
        lastCycleTimeTaken = duration;
        return madeChanged;
    }

    public static boolean iteration(SimplificationData data) {
        // TODO: Better debug lines
        // Reset debug lines
//        resetDebug(data);
//        data.debugLines.put(Color.blue, new GeometryList<>());
//        data.debugArrows.put(Color.blue, new GeometryList<>());

        // Find moves
        var moves = new ArrayList<>(IDecider.deciders.get(data.deciderType).findMoves(data));
        var madeChanges = moves.size() > 0;

        // Reset Debug values
        totalMovesMade += moves.size();
        lastCycleMovesMade = moves.size();
        lastCycleAreaEffected = 0;
        lastCycleVerticesRemoved = 0;

        // Apply found moves
        while (moves.size() > 0) {
            var move = moves.remove(0);
            applyMove(data, move);
        }


        //data.innerDifference = Util.calculateSymmetricDifference(polygon, data.original);
        //data.outerDifference = Util.calculateSymmetricDifference(data.original, polygon);

//        var inner = data.innerDifference.stream().mapToDouble(Polygon::areaUnsigned).sum();
//        var outer = data.outerDifference.stream().mapToDouble(Polygon::areaUnsigned).sum();
//        System.out.println("innerDifference = " + inner);
//        System.out.println("outerDifference = " + outer);

        return madeChanges;
    }

    public static void applyMove(SimplificationData data, Decision move) {
        var configuration = move.configuration;

        totalAreaEffected += move.removeArea;
        lastCycleAreaEffected += move.removeArea;

        // TODO: Better debug lines
        //data.debugArrows.get(Color.blue).add(configuration.inner.clone());

        var configurationIndex = configuration.index;
        var removed = configuration.performMove(move.type, move.removeArea, move.requiresCleanup);

        //data.debugLines.get(Color.blue).add(configuration.inner.clone());

        totalVerticesRemoved += removed.size();
        lastCycleVerticesRemoved += removed.size();

        data.configurations.forEach(c -> {
            removed.forEach(i -> {
                if (i <= c.index && c != configuration) {
                    c.index--;
                }
            });
        });

        // Collected affected configurations around moved edge
        var affectedConfigurations = new ArrayList<Configuration>(5);
        for (var i = configurationIndex - 2; i <= configurationIndex + 2; i++) {
            var index = (i + data.configurations.size()) % data.configurations.size();
            affectedConfigurations.add(data.configurations.get(index));
        }

        // Reset all configurations in affected area
        // TODO: minmax doesnt work when overlapping list end-start
        var firstIndex = -1;
        var lastIndex = -1;
        for (var affected : affectedConfigurations) {
            if (affected.index == -1 || DoubleUtil.close(affected.inner.length(), 0)) {
                data.configurations.remove(affected);
                continue;
            }

            affected.init();

            if (firstIndex == -1) firstIndex = affected.index;
            lastIndex = affected.index;
        }

        for (int i = 0; i < data.configurations.size(); i++) {
            var c = data.configurations.get(i);

            assert c.inner.getStart() == data.polygon.vertex(i) : "index for " + i + " not correct (" + c.index + ")";
            assert c.index == i : c.index + " != " + i + " for config: " + c;
        }

        var start = (firstIndex - 2);
        var end = lastIndex + 2;
        // 7 & 3 -> 7 & 10
        if (lastIndex < firstIndex)
            end += data.configurations.size();
        for (var i = start; i <= end; i++) {
            var index = (i + data.configurations.size()) % data.configurations.size();
            data.initSpecialPairs(index);
        }

        //TODO: Blocking numbers
    }

    //region debug

    public static void resetDebug(Data data) {
        // TODO: Better debug lines
//        data.debugLines.clear();
//        data.debugArrows.clear();
    }

    public static void drawDebug(SimplificationData data, DrawPanel panel) {
        if (data.polygon == null) return;

        var configurationList = data.configurations;
        if (configurationList == null)
            return;

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

    private static void drawBlockingVectors(DrawPanel panel, List<Configuration> list, Color color) {
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

    private static void drawMovementDirectionDebugLines(Data data, List<Configuration> list, Color outwardsColor, Color inwardsColor) {
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

    private static void drawDebugPositiveContractions(DrawPanel panel, List<Configuration> list, Color contractionColor, Color directionColor) {
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

    private static void drawDebugNegativeContractions(DrawPanel panel, List<Configuration> list, Color contractionColor, Color directionColor) {
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

    private static void drawDebugConvexityArcs(DrawPanel panel, List<Configuration> list, Color convexColor, Color reflexColor) {
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

    private static void drawDebugConvexityLines(DrawPanel panel, List<Configuration> list, Color convexColor, Color reflexColor) {
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
