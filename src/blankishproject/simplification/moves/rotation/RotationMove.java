package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.moves.Move;
import blankishproject.simplification.moves.MoveType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

public class RotationMove extends Move {

    protected final Configuration configuration;
    protected final OrientationSet orientations;
    protected final Vector rotationPoint;

    // Precalculated values
    protected Vector direction;
    protected LineSegment rotation;
    protected double area = 0.0;

    public RotationMove(Configuration configuration, Vector rotationPoint, OrientationSet orientations) {
        this.configuration = configuration;
        this.rotationPoint = rotationPoint;
        this.orientations = orientations;

        this.rotation = calculateRotation();
        if (this.rotation != null) {
            this.area = calculateArea();
        }
    }

    //region getters and setters
    @Override
    public MoveType getType() {
        return MoveType.ROTATION;
    }

    @Override
    public boolean isValid() {
        return rotation != null;
    }

    @Override
    public double getAffectedArea() {
        return area;
    }

    public LineSegment getRotation() {
        return rotation;
    }
    //endregion getters and setters

    //region applying move
    @Override
    public void applyForArea(double area) {
        assert DoubleUtil.close(area, this.area) : "Cannot partially apply PairMove: " + area + " != " + this.area;
        apply();
    }

    @Override
    public void apply() {
        configuration.inner.getEnd().set(rotation.getEnd());
        configuration.inner.getStart().set(rotation.getStart());
        rotation = null;
    }
    //endregion applying move

    //region calculation
    protected LineSegment calculateRotation() {
        var configurationDirection = Vector.subtract(configuration.next.getEnd(), configuration.inner.getStart());
        var closestOrientation = orientations.getClosest(configurationDirection.computeClockwiseAngleTo(Vector.up()));
        direction = closestOrientation.getDirection();

        var resultLine = new Line(rotationPoint, direction);

        var prevIntersections = resultLine.intersect(configuration.previous);
        if (!(prevIntersections.size() == 1 && prevIntersections.get(0) instanceof Vector)) {
            return null;
        }
        var nextIntersections = resultLine.intersect(configuration.next);
        if (!(nextIntersections.size() == 1 && nextIntersections.get(0) instanceof Vector)) {
            return null;
        }

        var newPrev = (Vector) prevIntersections.get(0);
        var newNext = (Vector) nextIntersections.get(0);
        return new LineSegment(newPrev, newNext);
    }

    private double calculateArea() {
        var prevTriangle = new Polygon(rotationPoint, configuration.previous.getEnd(), rotation.getStart());
        var nextTriangle = new Polygon(rotationPoint, configuration.next.getStart(), rotation.getEnd());
        return prevTriangle.areaUnsigned() + nextTriangle.areaUnsigned();
    }

}
