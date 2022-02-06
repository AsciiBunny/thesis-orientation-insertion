package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.Move;
import blankishproject.simplification.moves.MoveType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

public abstract class RotationMove extends Move {

    protected final SimplificationData data;
    protected final OrientationSet orientations;

    // Precalculated values
    protected Vector direction;
    protected LineSegment rotation;
    protected Vector rotationPoint;
    protected double area = 0.0;

    public RotationMove(SimplificationData data, Configuration configuration, OrientationSet orientations) {
        super(configuration);
        this.data = data;
        this.orientations = orientations;

        if (this.configuration.isInnerReflex() || this.configuration.isInnerConvex()) {
            return;
        }

        calculateRotation();
        if (this.rotation != null) {
            this.area = calculateArea(this.rotation, this.rotationPoint);
        }
        if (this.area <= DoubleUtil.EPS) {
            rotation = null;
        }
    }

    //region getters and setters
    @Override
    public MoveType getType() {
        return MoveType.ROTATION;
    }

    @Override
    public boolean isValid() {
        return rotation != null && !isLimited();
    }

    @Override
    public double getAffectedArea() {
        return area;
    }

    private boolean isLimited() {
        return configuration.index % (data.rotationDistance + 1) > 0;
    }

    @Override
    public double getCompensationArea() {
        var prevTriangle = new Polygon(rotationPoint, configuration.previous.getEnd(), rotation.getStart());
        var nextTriangle = new Polygon(rotationPoint, configuration.next.getStart(), rotation.getEnd());

        var prevArea = prevTriangle.areaUnsigned();
        var nextArea = nextTriangle.areaUnsigned();

        var prevPoint = new LineSegment(rotation.getStart(), rotationPoint).getPointAlongPerimeter(0.5);
        var nextPoint = new LineSegment(rotationPoint, rotation.getEnd()).getPointAlongPerimeter(0.5);

        if (configuration.isInsidePolygon(prevPoint)) {
            prevArea *= -1;
        }
        if (configuration.isInsidePolygon(nextPoint)) {
            nextArea *= -1;
        }

        return prevArea + nextArea;
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
    protected void calculateRotation() {
        var largestArea = -1.0;
        LineSegment largestRotation = null;
        Vector largestRotationPoint = null;

        for (OrientationSet.Orientation orientation : orientations) {
            direction = orientation.getDirection();

            var innerDirection = configuration.inner.getDirection();
            innerDirection.normalize();
            direction.normalize();
            if (innerDirection.isApproximately(direction) || innerDirection.isApproximately(Vector.multiply(-1, direction)))
                continue;

            var rotationPoint = getRotationPoint(orientation);
            var resultLine = new Line(rotationPoint, direction);

            var prevIntersections = resultLine.intersect(configuration.previous);
            Vector newPrev;
            if (prevIntersections.size() != 1) {
                continue;
            } else if (prevIntersections.get(0) instanceof Vector) {
                newPrev = (Vector) prevIntersections.get(0);
            } else if (prevIntersections.get(0) instanceof LineSegment) {
                newPrev = ((LineSegment) prevIntersections.get(0)).getEnd();
            } else {
                continue;
            }

            var nextIntersections = resultLine.intersect(configuration.next);
            Vector newNext;
            if (nextIntersections.size() != 1) { //&& nextIntersections.get(0) instanceof Vector)
                continue;
            } else if (nextIntersections.get(0) instanceof Vector) {
                newNext = (Vector) nextIntersections.get(0);
            } else if (nextIntersections.get(0) instanceof LineSegment) {
                newNext = ((LineSegment) nextIntersections.get(0)).getStart();
            } else {
                continue;
            }

            var newRotation = new LineSegment(newPrev, newNext);
            var newArea = calculateArea(newRotation, rotationPoint);

            if (newArea > largestArea) {
                largestArea = newArea;
                largestRotation = newRotation;
                largestRotationPoint = rotationPoint;
            }
        }

        this.rotationPoint = largestRotationPoint;
        this.rotation = largestRotation;
    }

    protected abstract Vector getRotationPoint(OrientationSet.Orientation orientation);

    private double calculateArea(LineSegment rotation, Vector rotationPoint) {
        var prevTriangle = new Polygon(rotationPoint, configuration.previous.getEnd(), rotation.getStart());
        var nextTriangle = new Polygon(rotationPoint, configuration.next.getStart(), rotation.getEnd());
        return prevTriangle.areaUnsigned() + nextTriangle.areaUnsigned();
    }

}
