package thesis.simplification.moves.moving;

import thesis.simplification.Configuration;
import thesis.simplification.moves.MoveType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;

public class NegativeNormalMove extends NormalMove {

    public NegativeNormalMove(Configuration configuration, Polygon polygon) {
        super(configuration, polygon);
    }

    @Override
    public MoveType getType() {
        return MoveType.NEGATIVE;
    }

    @Override
    public double getCompensationArea() {
        return - getArea();
    }

    /**
     * Calculates the new inner edge for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    @Override
    protected LineSegment calculateContraction() {
        if (configuration.isInnerReflex())
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
        return configuration.getInwardsNormal();
    }

    /**
     * Calculates the distance for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    @Override
    protected double calculateDistance() {
        var previousDistance = configuration.isStartConvex() ? configuration.getInnerTrack().distanceTo(configuration.previous.getStart()) : Integer.MAX_VALUE;
        var nextDistance = configuration.isEndConvex() ? configuration.getInnerTrack().distanceTo(configuration.next.getEnd()) : Integer.MAX_VALUE;

        return Math.min(previousDistance, nextDistance);
    }
}
