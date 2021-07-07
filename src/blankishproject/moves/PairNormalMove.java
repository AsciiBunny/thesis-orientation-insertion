package blankishproject.moves;

import blankishproject.Simplification;
import blankishproject.deciders.Decision;
import blankishproject.edgelist.Configuration;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import static blankishproject.Util.undirectedEquals;

public class PairNormalMove extends Move {

    protected final Configuration configuration;
    protected final Configuration pairedConfiguration;
    protected final NormalMove move;
    protected final NormalMove pairedMove;

    protected boolean isValid;
    protected double area;

    public PairNormalMove(Configuration configuration, Configuration pairedConfiguration, NormalMove move, NormalMove pairedMove, Polygon polygon) {
        this.configuration = configuration;
        this.pairedConfiguration = pairedConfiguration;
        this.move = move;
        this.pairedMove = pairedMove;

        calculateValidity();
        if (isValid) {
            calculateArea();
        }
    }


    @Override
    public MoveType getType() {
        return move.getType() == MoveType.POSITIVE ? MoveType.POSITIVE_PAIR : MoveType.NEGATIVE_PAIR;
    }

    public boolean isValid() {
        return isValid;
    }

    @Override
    public double getAffectedArea() {
        return area;
    }

    @Override
    public void applyForArea(double area) {
        assert DoubleUtil.close(area, this.area) : "Cannot partially apply PairMove: " + area + " != " + this.area;
        apply();
    }

    @Override
    public void apply() {
        var decision = new Decision(configuration, move.getType(), area, true);
        Simplification.applyMove(configuration.data, decision);

        var pairedDecision = new Decision(pairedConfiguration, pairedMove.getType(), area, true);
        Simplification.applyMove(configuration.data, pairedDecision);

    }

    private void calculateValidity() {
        // The two configurations should share an edge
        var sharedEdge = undirectedEquals(configuration.next, pairedConfiguration.previous);

        // The shared edge cannot be completely convex or reflex
        var validReflexity = (configuration.isEndReflex() && pairedConfiguration.isStartConvex())
                || (configuration.isEndConvex() && pairedConfiguration.isStartReflex());

        // The affected areas should be free of blocking points
        // TODO: calculate blocking numbers for expected areas
        var noBlocking = true;

        // If all these cases hold, this PairNormalMove is likely to be valid
        isValid = sharedEdge && validReflexity && noBlocking;
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
        var pairedDistance = sharedLength - distance;

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
}
