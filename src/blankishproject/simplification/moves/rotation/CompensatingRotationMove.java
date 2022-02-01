package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.DoubleUtil;

public class CompensatingRotationMove extends RotationMove {

    public CompensatingRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        var prevAngle = configuration.isStartConvex() ? 360 - configuration.getStartAngle() : configuration.getStartAngle();
        var nextAngle = configuration.isEndConvex() ? 360 - configuration.getEndAngle() : configuration.getEndAngle();

        var rotationAngle = orientation.getDirection().computeCounterClockwiseAngleTo(this.configuration.inner.getDirection());
        if (rotationAngle > 90)
            rotationAngle = Vector.multiply(-1.0, orientation.getDirection()).computeCounterClockwiseAngleTo(this.configuration.inner.getDirection());

        var prevSegmentRatio = Math.sin(rotationAngle) / Math.sin(prevAngle);
        var nextSegmentRatio = Math.sin(rotationAngle) / Math.sin(nextAngle);

        var k = Math.sin(rotationAngle);

        var a = prevSegmentRatio * k - nextSegmentRatio * k;
        var b = 2 * nextAngle * k;
        var c = - nextAngle * k;

        var innerSegmentRatio = DoubleUtil.solveQuadraticEquationForSmallestPositive(a, b, c, DoubleUtil.EPS);

        var point = configuration.inner.getPointAlongPerimeter(innerSegmentRatio);
        return point;
    }
}
