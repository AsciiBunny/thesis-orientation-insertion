package blankishproject.simplification.moves.moving;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.moves.MoveType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.LineSegment;

public class PositiveNormalMove extends NormalMove {

    public PositiveNormalMove(Configuration configuration, Polygon polygon) {
        super(configuration, polygon);
    }

    @Override
    public MoveType getType() {
        return MoveType.POSITIVE;
    }

    @Override
    public double getCompensationArea() {
        return getArea();
    }

    /**
     * Calculates the new inner edge for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    @Override
    protected LineSegment calculateContraction() {
        if (configuration.isInnerConvex())
            return null;

        var normal = Vector.multiply(distance, direction);

        return calculateNormalContraction(normal);
    }

    /**
     * Calculates the direction for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    @Override
    protected Vector calculateDirection() {
        return configuration.getOutwardsNormal();
    }

    /**
     * Calculates the distance for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    @Override
    protected double calculateDistance() {
        var previousDistance = configuration.isStartReflex() ? configuration.getInnerTrack().distanceTo(configuration.previous.getStart()) : Integer.MAX_VALUE;
        var nextDistance = configuration.isEndReflex() ? configuration.getInnerTrack().distanceTo(configuration.next.getEnd()) : Integer.MAX_VALUE;

        return Math.min(previousDistance, nextDistance);
    }


}
