package blankishproject.simplification;

import blankishproject.Data;
import blankishproject.GeometryList;
import blankishproject.Util;
import blankishproject.simplification.deciders.Decision;
import blankishproject.simplification.deciders.IDecider;
import blankishproject.ui.DrawPanel;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometryrendering.glyphs.ArrowStyle;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simplification {

    private static final int affectedRange = 4;


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
        data.canceled = false;
        for (int cycle = 0; cycle < cycles; cycle++) {
            if (data.canceled) break;
            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            before = after;

            data.progress = cycle;
            data.dialog.update();

            if (done) break;
        }

        data.innerDifference = Util.calculateSymmetricDifference(polygon, data.original);
        data.outerDifference = Util.calculateSymmetricDifference(data.original, polygon);
    }

    public static void runUntilLeft(Data data, int left) {
        var polygon = data.simplificationData.polygon;
        var before = polygon.vertexCount();
        data.canceled = false;
        while (before > left) {
            if (data.canceled) break;

            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            data.progress = data.maxProgress - after + left;
            data.dialog.update();
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

    public static void applyMove(SimplificationData data, Decision decision) {
        var decisionConfiguration = decision.configuration;
        var decisionIndex = decisionConfiguration.index;

        totalAreaEffected += decision.removeArea;
        lastCycleAreaEffected += decision.removeArea;

        // TODO: Better debug lines
        //data.debugArrows.get(Color.blue).add(configuration.inner.clone());


        decision.move.applyForArea(decision.removeArea);
        //List<Integer> removed = decision.requiresCleanup ? decision.configuration.moveCleanup() : Collections.emptyList();
        if (!decision.requiresCleanup) return;
        //System.out.println("removed = " + removed);

        //data.debugLines.get(Color.blue).add(configuration.inner.clone());

        //totalVerticesRemoved += removed.size();
        //lastCycleVerticesRemoved += removed.size();


        // Collect affected configurations around moved edge
        var affectedIndexes = new ArrayList<Integer>(affectedRange * 2 + 1);
        for (var i = decisionIndex - affectedRange; i <= decisionIndex + affectedRange; i++) {
            var index = (i + data.configurations.size()) % data.configurations.size();
            affectedIndexes.add(index);
        }


        // Sort indexes backwards so indexed removals work correctly
        affectedIndexes.sort(Collections.reverseOrder());


        // TODO: Keep track of removed vertices
        // TODO: COMMENT THIS
        var removed = new ArrayList<Vector>();
        var left = validityCheck(data, affectedIndexes, removed);
        for (var index : left) {
            var configuration = new Configuration(data.polygon, index);
            data.configurations.set(index, configuration);
        }
        // Second cleanup for straight-lines that popup after 0-lengths are removed
        left = validityCheck(data, left, removed);

        resetIndices(data, left);

        //TODO: Blocking numbers
        updateBlockingNumbers(data, removed);
    }

    private static List<Integer> validityCheck(SimplificationData data, List<Integer> affected, List<Vector> removed) {
        var leftIndexes = new ArrayList<Integer>(affectedRange * 2 + 1);
        for (var index : affected) {
            var configuration = data.configurations.get(index);

            if (configuration.wasInvalidated()) {
                var vector = data.removeAtIndex(index);
                removed.add(vector);
                // Update indexes in configurations
                data.configurations.forEach(c -> {
                    if (index < c.index) {
                        c.index--;
                    }
                });
                // Update indexes in leftIndexes appropriately
                for (int i = 0; i < leftIndexes.size(); i++) {
                    if (index <= leftIndexes.get(i)) {
                        leftIndexes.set(i, leftIndexes.get(i) - 1);
                    }
                }
            } else {
                leftIndexes.add(configuration.index);
            }
        }

        // region asserts
        // Check if clean up left all indices correct
        for (int i = 0; i < data.configurations.size(); i++) {
            var configuration = data.configurations.get(i);

            assert configuration.index == i : configuration.index + " != " + i + " for config: " + configuration;
            assert !configuration.wasInvalidated();
        }

        assert data.polygon.vertices().size() == data.configurations.size() : data.polygon.vertices().size() + " != " + data.configurations.size();
        assert data.polygon.vertices().size() == data.positiveMoves.size() : data.polygon.vertices().size() + " != " + data.positiveMoves.size();
        assert data.polygon.vertices().size() == data.negativeMoves.size() : data.polygon.vertices().size() + " != " + data.negativeMoves.size();
        assert data.polygon.vertices().size() == data.positivePairMoves.size() : data.polygon.vertices().size() + " != " + data.positivePairMoves.size();
        assert data.polygon.vertices().size() == data.negativePairMoves.size() : data.polygon.vertices().size() + " != " + data.negativePairMoves.size();
        //endregion asserts

        return leftIndexes;
    }

    private static void resetIndices(SimplificationData data, List<Integer> indices) {
        // completely reset and reinitialize all configurations that did not get deleted
        for (var index : indices) {
            data.resetAtIndex(index);
        }

        for (var index : indices) {
            data.resetSpecialPairs(index);
            var offsetIndex = (index - 2 + data.configurations.size()) % data.configurations.size();
            data.resetSpecialPairs(offsetIndex);
        }
    }

    private static void updateBlockingNumbers(SimplificationData data, ArrayList<Vector> removed) {
        data.positiveMoves.forEach(move -> move.updateBlockingVectors(removed));
        data.negativeMoves.forEach(move -> move.updateBlockingVectors(removed));
        // TODO: Complementary moves
    }

        //region debug drawing

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
            drawDebugPositiveContractions(panel, data, Color.red, Color.green);
        if (data.drawNegativeContractions)
            drawDebugNegativeContractions(panel, data, Color.red, Color.green);

        if (data.drawBlockingPoints)
            drawBlockingVectors(panel, data, Color.cyan);
    }

    private static void drawBlockingVectors(DrawPanel panel, SimplificationData data, Color color) {
        panel.setStroke(color, 3, Dashing.dashed(3));
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);

        for (var move : data.positiveMoves) {
            if (move.hasContraction()) {
                var blockers = move.getBlockingVectors();

                blockers.forEach(vector -> panel.draw(new LineSegment(move.configuration.inner.getPointAlongPerimeter(0.5), vector)));
            }
        }

        for (var move : data.negativeMoves) {
            if (move.hasContraction()) {
                var blockers = move.getBlockingVectors();

                blockers.forEach(vector -> panel.draw(new LineSegment(move.configuration.inner.getPointAlongPerimeter(0.5), vector)));
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

    private static void drawDebugPositiveContractions(DrawPanel panel, SimplificationData data, Color contractionColor, Color directionColor) {
        for (var move : data.positiveMoves) {
            if (!move.hasValidContraction()) continue;
            var contraction = move.getContraction();
            if (contraction != null) {
                panel.setStroke(contractionColor, 3, Dashing.SOLID);
                panel.draw(contraction);

                var a = move.configuration.inner.getPointAlongPerimeter(0.5);
                panel.setStroke(directionColor, 3, Dashing.dashed(3));
                panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)));
                panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);

//                panel.setStroke(directionColor, 3, Dashing.dashed(3));
//                panel.draw(move.configuration.inner);
//
//                panel.setStroke(extensionColor, 3, Dashing.dashed(3));
//                panel.draw(new LineSegment(contraction.getEnd(), move.configuration.next.getEnd()));
//                panel.draw(new LineSegment(contraction.getStart(), move.configuration.previous.getStart()));

                var area = move.getArea();
                panel.setTextStyle(TextAnchor.BASELINE_CENTER, 16);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)).getPointAlongPerimeter(0.5), String.format("%.2f", area));
            }
        }
    }

    private static void drawDebugNegativeContractions(DrawPanel panel, SimplificationData data, Color contractionColor, Color directionColor) {
        for (var move : data.negativeMoves) {
            if (!move.hasValidContraction()) continue;
            var contraction = move.getContraction();
            if (contraction != null) {
                panel.setStroke(contractionColor, 3, Dashing.SOLID);
                panel.draw(contraction);

                var a = move.configuration.inner.getPointAlongPerimeter(0.5);
                panel.setStroke(directionColor, 3, Dashing.dashed(3));
                panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);
                panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)));
                panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);

                var area = move.getArea();
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
