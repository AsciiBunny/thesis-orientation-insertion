package blankishproject.simplification.moves.moving;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.Simplification;
import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.deciders.Decision;
import blankishproject.simplification.moves.Move;
import blankishproject.simplification.moves.MoveType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static blankishproject.Util.undirectedEquals;

public class PairNormalMove extends Move {

    protected final SimplificationData data;
    public final Configuration pairedConfiguration;
    public final NormalMove move;
    public final NormalMove pairedMove;

    protected boolean isValid;
    protected double area;

    protected List<LineSegment> blockingEdges;

    public PairNormalMove(SimplificationData data, Configuration configuration, boolean invalid) {
        super(configuration);
        this.data = data;

        this.pairedConfiguration = null;
        this.move = null;
        this.pairedMove = null;

        this.isValid = false;
        this.area = 0;
        this.blockingEdges = Collections.emptyList();
    }

    public PairNormalMove(Configuration configuration, Configuration pairedConfiguration, NormalMove move, NormalMove pairedMove, SimplificationData data) {
        super(configuration);
        this.data = data;
        this.pairedConfiguration = pairedConfiguration;
        this.move = move;
        this.pairedMove = pairedMove;

        this.isValid = calculateValidity();
        if (isValid)
            calculateArea();
        // calculateArea can invalidate
        if (isValid && area > 0)
            this.blockingEdges = calculateBlockingVectors(data.polygon);
    }


    @Override
    public MoveType getType() {
        return move.getType() == MoveType.POSITIVE ? MoveType.POSITIVE_PAIR : MoveType.NEGATIVE_PAIR;
    }

    public boolean isValid() {
        return isValid && blockingEdges.size() == 0;
    }

    @Override
    public double getAffectedArea() {
        return area * 2;
    }

    @Override
    public double getCompensationArea() {
        return 0;
    }

    @Override
    public void applyForArea(double area) {
        assert DoubleUtil.close(area, this.area * 2) : "Cannot partially apply PairMove: " + area + " != " + this.area * 2;
        apply();
    }

    @Override
    public void apply() {
        var decision = new Decision(configuration, move, area, true);
        Simplification.applyMove(data, decision);

        var newPairedConfiguration = data.configurations.get(pairedConfiguration.index);
        var pairedDecision = new Decision(newPairedConfiguration, pairedMove, area, true);
        Simplification.applyMove(data, pairedDecision);
    }

    private boolean calculateValidity() {
        // The two configurations should share an edge
        var sharedEdge = undirectedEquals(configuration.next, pairedConfiguration.previous);

        // The shared edge cannot be completely convex or reflex
        var validReflexivity = (configuration.isEndReflex() && pairedConfiguration.isStartConvex())
                || (configuration.isEndConvex() && pairedConfiguration.isStartReflex());

        // If all these cases hold, this PairNormalMove is likely to be valid
        return sharedEdge && validReflexivity;
    }

    private void calculateArea() {
        // Collect needed variables
        final var sharedLength = configuration.next.length();
        final var sharedDirection = configuration.next.getDirection();

        final var height1 = move.getDistance();
        final var height2 = pairedMove.getDistance();

        final var innerLength1 = configuration.inner.length();
        final var innerLength2 = pairedConfiguration.inner.length();

        final var outerLength1 = move.getContraction().length();
        final var outerLength2 = pairedMove.getContraction().length();

        // Todo: better naming of ratios/angles
        final var ratio1 = (outerLength1 - innerLength1) / height1;
        final var ratio2 = (outerLength2 - innerLength2) / height2;

        final var angle1 = Vector.dotProduct(move.getDirection(), sharedDirection);
        final var angle2 = Vector.dotProduct(pairedMove.getDirection(), Vector.multiply(-1, sharedDirection));

        // Use variables to calculate the a,b,c for a quadratic equation
        var a = angle1 * angle1 * ratio1 - angle2 * angle2 * ratio2;
        var b = 2 * angle1 * innerLength1 + 2 * angle2 * angle2 * sharedLength * ratio2 + 2 * angle2 * innerLength2;
        var c = -angle2 * angle2 * sharedLength * sharedLength * ratio2 - 2 * angle2 * innerLength2 * sharedLength;

        var distance = DoubleUtil.solveQuadraticEquationForSmallestPositive(a, b, c, DoubleUtil.EPS);

        //assert distance > 0 && distance < sharedLength : distance + " not in <0.0 , " + sharedLength + ">";
        //assert pairedDistance > 0 && pairedDistance < sharedLength : pairedDistance + " not in <0.0 ," + sharedLength + ">";
        //assert !DoubleUtil.close(distance, 0) && !DoubleUtil.close(pairedDistance, 0);

        if (distance * angle1 < 0 || DoubleUtil.close(distance * angle1, 0)) {
            isValid = false;
            return;
        }

        // If found distance larger than move would allow, adjust target area down
        if (distance * angle1 < move.distance - DoubleUtil.EPS) {
            // Normal case
            this.area = move.getAreaForDistance(distance * angle1);
        } else {
            // Edge-case
            this.area = move.getArea();
        }

        // If determined target area is larger then paired move would allow, adjust down too
        if (this.area > Math.abs(pairedMove.getArea() + DoubleUtil.EPS)) {
            // same Edge-case but for pairedmove
            this.area = pairedMove.getArea();
        }

    }
    //endregion

    //region Blocking Vectors

    /**
     * Find all blocking vectors for this contraction.
     * <br> Complexity O(n)
     */
    protected List<LineSegment> calculateBlockingVectors(Polygon polygon) {
        var edges = move.calculateBlockingVectors(polygon, move.getForArea(area));
        edges.addAll(pairedMove.calculateBlockingVectors(polygon, pairedMove.getForArea(area)));
        return edges;
    }

    public void updateBlockingVectors(List<LineSegment> removed, List<LineSegment> changed) {
        if (!this.isValid) return;

        var outer = move.getForArea(area);
        var contractionArea = move.getContractionArea(outer);
        var boundary = move.getBoundary();

        var pairedOuter = pairedMove.getForArea(area);
        var pairedContractionArea = pairedMove.getContractionArea(pairedOuter);
        var pairedBoundary = pairedMove.getBoundary();

        blockingEdges = blockingEdges.stream().filter(edge -> {
            if (removed.stream().anyMatch(removedEdge -> undirectedEquals(removedEdge, edge)))
                return false;

            if (move.isBlocking(contractionArea, boundary, edge))
                return true;
            if (pairedMove.isBlocking(pairedContractionArea, pairedBoundary, edge))
                return true;

            return false;
        }).collect(Collectors.toList());

        changed.forEach(changedEdge -> {
            if (move.isBlocking(contractionArea, boundary, changedEdge))
                blockingEdges.add(changedEdge);
            else if (pairedMove.isBlocking(pairedContractionArea, pairedBoundary, changedEdge))
                blockingEdges.add(changedEdge);
        });
    }
    //endregion Blocking Vectors

}
