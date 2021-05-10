package blankishproject.moves;

import blankishproject.edgelist.Configuration;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import static blankishproject.Util.undirectedEquals;

public class PairNormalMove extends Move {

    protected final Configuration configuration;
    protected final Configuration pairedConfiguration;
    protected final NormalMove move;
    protected final NormalMove pairedMove;

    protected boolean isValid;
    protected double distance;
    protected double pairedDistance;
    protected double area;

    public PairNormalMove(Configuration configuration, Configuration pairedConfiguration, NormalMove move, NormalMove pairedMove, Polygon polygon) {
        this.configuration = configuration;
        this.pairedConfiguration = pairedConfiguration;
        this.move = move;
        this.pairedMove = pairedMove;

        calculateValidity();
        if (isValid) {
            calculateDistances();
            calculateArea();
        }
    }

    public boolean isValid() {
        return isValid;
    }

    private void calculateValidity() {
        var sharedEdge = undirectedEquals(pairedConfiguration.next, configuration.previous) && (pairedConfiguration.isEndReflex() && configuration.isStartConvex() || pairedConfiguration.isEndConvex() && configuration.isStartReflex());
        var noBlocking = true;
        isValid = sharedEdge && noBlocking;
    }

    private void calculateDistances() {
        // Collect needed variables
        final var sharedLength = configuration.next.length();

        final var height1 = move.getDistance();
        final var height2 = pairedMove.getDistance();

        final var innerLength1 = configuration.inner.length();
        final var innerLength2 = pairedConfiguration.inner.length();

        final var outerLength1 = move.getContraction().length();
        final var outerLength2 = pairedMove.getContraction().length();

        final var ratio1 = (outerLength1 - innerLength1) / height1;
        final var ratio2 = (outerLength2 - innerLength2) / height2;

        // Adjust angles to angle within right triangle so sin can be used to scale on shared edge
        // Right angle should give 1 as value
        var rawAngle1 = configuration.getEndAngle();
        rawAngle1 = rawAngle1 > Math.PI ? 2 * Math.PI - rawAngle1 : rawAngle1;
        rawAngle1 = rawAngle1 > Math.PI / 2 ? Math.PI - rawAngle1 : rawAngle1;
        var rawAngle2 = pairedConfiguration.getStartAngle();
        rawAngle2 = rawAngle2 > Math.PI ? 2 * Math.PI - rawAngle2 : rawAngle2;
        rawAngle2 = rawAngle2 > Math.PI / 2 ? Math.PI - rawAngle2 : rawAngle2;

        final var angle1 = 1 / Math.sin(rawAngle1);
        final var angle2 = 1 / Math.sin(rawAngle2);

        // Use variables to calculate the a,b,c for a quadratic equation
        var a = angle1 * angle1 * innerLength1 * ratio1 - angle2 * angle2 * innerLength2 * ratio2;
        var b = angle1 * innerLength1 + angle2 * innerLength2 + 2 * angle2 * angle2 * innerLength2 * ratio2 * sharedLength;
        var c = -angle2 * angle2 * innerLength2 * sharedLength * sharedLength * ratio2 - angle2 * innerLength2 * sharedLength;

        var distance1 = DoubleUtil.solveQuadraticEquationForSmallestPositive(a, b, c, DoubleUtil.EPS);
        var distance2 = sharedLength - distance1;

        assert distance1 > 0 && distance1 < sharedLength : distance1 + " not in [0.0 , " + sharedLength + "]";
        assert distance2 > 0 && distance2 < sharedLength : distance2 + " not in [0.0 ," + sharedLength + "]";
        assert !DoubleUtil.close(distance1, 0) && !DoubleUtil.close(distance2, 0);

        distance = Math.min(distance1, move.distance);
        pairedDistance = Math.min(distance1, pairedMove.distance);
    }

    private void calculateArea() {
        // Find areas of potential moves. Take into account that move cant be larger then full contraction.
        var area1 = move.getAreaForDistance(distance);
        var area2 = pairedMove.getAreaForDistance(pairedDistance);

        area = Math.min(area1, area2);
    }

    public boolean isMoveContraction() {
        return DoubleUtil.close(distance, move.getDistance());
    }

    public boolean isPairedMoveContraction() {
        return DoubleUtil.close(pairedDistance, pairedMove.getDistance())
                || !isMoveContraction();
    }
}
