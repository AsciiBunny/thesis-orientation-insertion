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
        var prevAngle = Vector.multiply(-1.0, configuration.previous.getDirection()).computeCounterClockwiseAngleTo(configuration.inner.getDirection());
        prevAngle = prevAngle > Math.PI ? 2 * Math.PI - prevAngle : prevAngle;
        var nextAngle = Vector.multiply(-1.0, configuration.inner.getDirection()).computeCounterClockwiseAngleTo(configuration.next.getDirection());
        nextAngle = nextAngle > Math.PI ? 2 * Math.PI - nextAngle : nextAngle;

        var rotationAngle = orientation.getDirection().computeCounterClockwiseAngleTo(this.configuration.inner.getDirection());
        if (rotationAngle > Math.PI)
            rotationAngle = 2 * Math.PI - rotationAngle;
        if (rotationAngle > 0.5 * Math.PI)
            rotationAngle = Math.PI - rotationAngle;

        var prevIntersectionAngle = Math.PI - rotationAngle - prevAngle;
        var nextIntersectionAngle = Math.PI - rotationAngle - nextAngle;

        var k = Math.sin(rotationAngle);
        var prevSegmentRatio = Math.sin(prevAngle);
        var nextSegmentRatio = Math.sin(nextAngle);
        var prevIntersectionRatio = Math.sin(prevIntersectionAngle);
        var nextIntersectionRatio = Math.sin(nextIntersectionAngle);

        var a = (k * prevSegmentRatio) / prevIntersectionRatio - (k * nextSegmentRatio) / nextIntersectionRatio;
        var b = (2 * k * nextSegmentRatio) / nextIntersectionRatio;
        var c = (-nextSegmentRatio * k) / nextIntersectionRatio;

        var innerSegmentRatio = DoubleUtil.solveQuadraticEquationForSmallestPositive(a, b, c, DoubleUtil.EPS);
        var point = configuration.inner.getPointAlongPerimeter(innerSegmentRatio);
        return point;
    }
}
