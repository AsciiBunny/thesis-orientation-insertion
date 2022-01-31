package blankishproject.simplification;

import blankishproject.Data;
import blankishproject.GeometryList;
import blankishproject.Util;
import blankishproject.simplification.deciders.Decision;
import blankishproject.simplification.deciders.IDecider;
import blankishproject.simplification.moves.moving.NormalMove;
import blankishproject.simplification.moves.moving.PairNormalMove;
import blankishproject.simplification.moves.rotation.RotationMove;
import blankishproject.ui.DrawPanel;
import blankishproject.ui.ProgressDialog;
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
import java.util.stream.Collectors;

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

    public static void initState(SimplificationData data, Polygon polygon, ProgressDialog dialog) {
        data.init(polygon.clone(), dialog);
    }

    public static void run(Data data) {
        timedIteration(data.simplificationData);

        calculateSymmetricDifference(data, data.simplificationData.polygon);
    }

    public static void run(Data data, int cycles, ProgressDialog dialog) {
        var polygon = data.simplificationData.polygon;
        var before = polygon.vertexCount();
        dialog.canceled = false;
        for (int cycle = 0; cycle < cycles; cycle++) {
            if (dialog.canceled) break;
            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            before = after;

            data.dialog.setProgress(cycle);

            if (done) break;
        }

        calculateSymmetricDifference(data, polygon);
    }

    public static void runUntilLeft(Data data, int left, ProgressDialog dialog) {
        var polygon = data.simplificationData.polygon;
        var beforeTotal = polygon.vertexCount();
        var before = polygon.vertexCount();
        dialog.canceled = false;
        while (before > left) {
            if (dialog.canceled) break;

            var done = !iteration(data.simplificationData);

            var after = polygon.vertexCount();
            if (before != after)
                System.out.println("Removed " + (before - after) + " vertices out of " + after);
            else
                System.out.println("Removed 0 vertices");

            data.dialog.setProgress(beforeTotal - after + left);
            before = after;

            if (done) break;
        }

        calculateSymmetricDifference(data, polygon);
    }

    public static void finish(Data data, ProgressDialog dialog) {
        runUntilLeft(data, 0, dialog);
    }

    public static boolean timedIteration(SimplificationData data) {
        long start = System.currentTimeMillis();
        var madeChanges = iteration(data);
        long end = System.currentTimeMillis();
        long duration = end - start;
        totalTimeTaken += duration;
        lastCycleTimeTaken = duration;
        return madeChanges;
    }

    public static boolean iteration(SimplificationData data) {
        // TODO: Better debug lines

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

        return madeChanges;
    }

    public static void applyMove(SimplificationData data, Decision decision) {
        var decisionConfiguration = decision.configuration;
        var decisionIndex = decisionConfiguration.index;

        totalAreaEffected += decision.removeArea;
        lastCycleAreaEffected += decision.removeArea;

        // TODO: Better debug lines

        decision.move.applyForArea(decision.removeArea);
        if (!decision.requiresCleanup) return;


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
        var removed = new ArrayList<LineSegment>();
        var left = validityCheck(data, affectedIndexes, removed);
        for (var index : left) {
            var configuration = new Configuration(data.polygon, index);
            data.configurations.set(index, configuration);
        }

        // collect changed edges
        var changed = left.stream().map(index -> data.polygon.edge(index)).collect(Collectors.toList());

        // Second cleanup for straight-lines that popup after 0-lengths are removed
        left = validityCheck(data, left, removed);

        resetIndices(data, left);

        //TODO: Blocking numbers
        updateBlockingNumbers(data, removed, changed);
        data.recalculateStaircases();
    }

    private static List<Integer> validityCheck(SimplificationData data, List<Integer> affected, List<LineSegment> removed) {
        var leftIndexes = new ArrayList<Integer>(affectedRange * 2 + 1);
        for (var index : affected) {
            var configuration = data.configurations.get(index);

            if (configuration.wasInvalidated()) {
                var edge = data.removeAtIndex(index);
                removed.add(edge);
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
//        for (int i = 0; i < data.configurations.size(); i++) {
//            var configuration = data.configurations.get(i);
//
//            assert configuration.index == i : configuration.index + " != " + i + " for config: " + configuration;
//            assert !configuration.wasInvalidated();
//        }

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

    private static void updateBlockingNumbers(SimplificationData data, List<LineSegment> removed, List<LineSegment> changed) {
        data.positiveMoves.forEach(move -> move.updateBlockingVectors(removed, changed));
        data.negativeMoves.forEach(move -> move.updateBlockingVectors(removed, changed));
        data.positivePairMoves.forEach(move -> move.updateBlockingVectors(removed, changed));
        data.negativePairMoves.forEach(move -> move.updateBlockingVectors(removed, changed));
    }

    private static void calculateSymmetricDifference(Data data, Polygon polygon) {
        data.innerDifference = Util.calculateSymmetricDifference(polygon, data.original);
        data.outerDifference = Util.calculateSymmetricDifference(data.original, polygon);

        var inner = data.innerDifference.stream().filter(p -> p.vertexCount() > 0).mapToDouble(Polygon::areaUnsigned).sum();
        var outer = data.outerDifference.stream().filter(p -> p.vertexCount() > 0).mapToDouble(Polygon::areaUnsigned).sum();
        System.out.println("innerDifference = " + inner);
        System.out.println("outerDifference = " + outer);
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

        if (data.drawForAll) {
            if (data.drawConvexityArcs)
                drawDebugConvexityArcs(panel, configurationList, Color.red, Color.green);
            if (data.drawConvexityEdges)
                drawDebugConvexityLines(panel, configurationList);

            if (data.drawPositiveContractions)
                drawDebugContractions(panel, Collections.unmodifiableList(data.positiveMoves));
            if (data.drawNegativeContractions)
                drawDebugContractions(panel, Collections.unmodifiableList(data.negativeMoves));

            if (data.drawPositivePairs)
                drawDebugComplementaryMoves(panel, data.positivePairMoves);
            if (data.drawNegativePairs)
                drawDebugComplementaryMoves(panel, data.negativePairMoves);

            if (data.drawBlockingPoints)
                drawBlockingVectors(panel, data);

            if (data.drawStartRotations)
                drawDebugRotations(panel, data.startRotationMoves);
            if (data.drawEndRotations)
                drawDebugRotations(panel, data.endRotationMoves);
            if (data.drawMiddleRotations)
                drawDebugRotations(panel, data.middleRotationMoves);

            drawDebugStaircases(panel, data);
        }


        if (data.selectedEdge > 0) {
            var index = data.selectedEdge;

            panel.setStroke(Color.cyan, 1, Dashing.dashed(1));
            panel.draw(data.polygon.edge(index));

            if (data.drawConvexityArcs)
                drawDebugConvexityArcs(panel, Collections.singletonList(data.configurations.get(index)), Color.red, Color.green);
            if (data.drawConvexityEdges)
                drawDebugConvexityLines(panel, Collections.singletonList(data.configurations.get(index)));

            if (data.drawPositiveContractions)
                drawDebugContractions(panel, Collections.singletonList(data.positiveMoves.get(index)));
            if (data.drawNegativeContractions)
                drawDebugContractions(panel, Collections.singletonList(data.negativeMoves.get(index)));

            if (data.drawPositivePairs)
                drawDebugComplementaryMoves(panel, Collections.singletonList(data.positivePairMoves.get(index)));
            if (data.drawNegativePairs)
                drawDebugComplementaryMoves(panel, Collections.singletonList(data.negativePairMoves.get(index)));

            if (data.drawStartRotations)
                drawDebugRotations(panel, Collections.singletonList(data.startRotationMoves.get(index)));
            if (data.drawEndRotations)
                drawDebugRotations(panel, Collections.singletonList(data.endRotationMoves.get(index)));
            if (data.drawMiddleRotations)
                drawDebugRotations(panel, Collections.singletonList(data.middleRotationMoves.get(index)));

            if (data.drawBlockingPoints) {
                panel.setStroke(Color.cyan, 3, Dashing.dashed(3));
                panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);

                var positiveMove = data.positiveMoves.get(index);
                if (positiveMove.hasContraction()) {
                    var blockers = positiveMove.getBlockingEdges();

                    blockers.forEach(edge -> panel.draw(new LineSegment(
                            positiveMove.configuration.inner.getPointAlongPerimeter(0.5),
                            edge.getPointAlongPerimeter(0.5))));
                }

                var negativeMove = data.negativeMoves.get(index);
                if (negativeMove.hasContraction()) {
                    var blockers = negativeMove.getBlockingEdges();

                    blockers.forEach(edge -> panel.draw(new LineSegment(
                            negativeMove.configuration.inner.getPointAlongPerimeter(0.5),
                            edge.getPointAlongPerimeter(0.5))));
                }

                panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);
            }
        }
    }

    private static void drawBlockingVectors(DrawPanel panel, SimplificationData data) {
        panel.setStroke(Color.cyan, 3, Dashing.dashed(3));
        panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);

        for (var move : data.positiveMoves) {
            if (move.hasContraction()) {
                var blockers = move.getBlockingEdges();

                blockers.forEach(edge -> panel.draw(new LineSegment(
                        move.configuration.inner.getPointAlongPerimeter(0.5),
                        edge.getPointAlongPerimeter(0.5))));
            }
        }

        for (var move : data.negativeMoves) {
            if (move.hasContraction()) {
                var blockers = move.getBlockingEdges();

                blockers.forEach(edge -> panel.draw(new LineSegment(
                        move.configuration.inner.getPointAlongPerimeter(0.5),
                        edge.getPointAlongPerimeter(0.5))));
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

    private static void drawDebugContractions(DrawPanel panel, List<NormalMove> moves) {
        for (var move : moves) {
            if (!move.hasValidContraction()) continue;
            var contraction = move.getContraction();
            drawDebugContraction(panel, move, contraction, move.getArea(), Color.red, Color.green);
        }
    }

    private static void drawDebugComplementaryMoves(DrawPanel panel, List<PairNormalMove> pairs) {
        for (var pair : pairs) {
            if (!pair.isValid()) continue;
            var c1 = pair.move.getForArea(pair.getAffectedArea() / 2);
            var c2 = pair.pairedMove.getForArea(pair.getAffectedArea() / 2);
            drawDebugContraction(panel, pair.move, c1, pair.getAffectedArea() / 2, Color.magenta, Color.pink);
            drawDebugContraction(panel, pair.pairedMove, c2, pair.getAffectedArea() / 2, Color.cyan, Color.pink);

            var a1 = pair.move.configuration.inner.getPointAlongPerimeter(0.5);
            var a2 = c1.getPointAlongPerimeter(0.5);
            var a = new LineSegment(a1, a2).getPointAlongPerimeter(0.5);
            var b1 = pair.pairedMove.configuration.inner.getPointAlongPerimeter(0.5);
            var b2 = c2.getPointAlongPerimeter(0.5);
            var b = new LineSegment(b1, b2).getPointAlongPerimeter(0.5);
            panel.setStroke(Color.pink, 3, Dashing.dashed(1));
            panel.draw(new LineSegment(a, b));
        }
    }

    private static void drawDebugContraction(DrawPanel panel, NormalMove move, LineSegment contraction, double area, Color moveColor, Color directionColor) {
        if (contraction != null) {
            panel.setStroke(moveColor, 3, Dashing.SOLID);
            panel.draw(contraction);

            var a = move.configuration.inner.getPointAlongPerimeter(0.5);
            panel.setStroke(directionColor, 3, Dashing.dashed(3));
            panel.setForwardArrowStyle(ArrowStyle.TRIANGLE_SOLID, 5);
            panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)));
            panel.setForwardArrowStyle(ArrowStyle.LINEAR, 0);

            panel.setTextStyle(TextAnchor.BASELINE_CENTER, 16);
            panel.draw(new LineSegment(a, contraction.getPointAlongPerimeter(0.5)).getPointAlongPerimeter(0.5), String.format("%.2f", area));
        }
    }

    private static void drawDebugRotations(DrawPanel panel, List<RotationMove> rotations) {
        for (var rotation : rotations) {
            if (!rotation.isValid()) continue;
            var newInner = rotation.getRotation();

            panel.setStroke(Color.pink, 3, Dashing.SOLID);
            panel.draw(newInner);
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

    private static void drawDebugConvexityLines(DrawPanel panel, List<Configuration> list) {
        for (var conf : list) {
            if (conf.isInnerConvex()) {
                panel.setStroke(Color.red, 3, Dashing.SOLID);
                panel.draw(conf.inner);
            } else if (conf.isInnerReflex()) {
                panel.setStroke(Color.green, 3, Dashing.SOLID);
                panel.draw(conf.inner);
            } else {
                panel.setStroke(Color.pink.darker(), 3, Dashing.SOLID);
                panel.draw(conf.inner);
            }
        }
    }

    private static void drawDebugStaircases(DrawPanel panel, SimplificationData data) {
        panel.setStroke(Color.red.darker(), 3, Dashing.SOLID);
        for (var staircase : data.staircases) {
            if (staircase.loops) {
                for (int i = 0; i <= staircase.end; i++) {
                    var edge = data.configurations.get(i);
                    panel.draw(edge.inner);
                }
                for (int i = staircase.start; i < data.configurations.size(); i++) {
                    var edge = data.configurations.get(i);
                    panel.draw(edge.inner);
                }
            } else {
                for (int i = staircase.start; i <= staircase.end; i++) {
                    var edge = data.configurations.get(i);
                    panel.draw(edge.inner);
                }
            }
        }
    }

    //endregion debug
}
