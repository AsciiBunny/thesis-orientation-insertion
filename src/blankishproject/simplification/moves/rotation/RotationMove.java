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

    protected final OrientationSet orientations;
    protected final Vector rotationPoint;

    // Precalculated values
    protected Vector direction;
    protected LineSegment rotation;
    protected double area = 0.0;

    public RotationMove(Configuration configuration, Vector rotationPoint, OrientationSet orientations) {
        super(configuration);
        this.rotationPoint = rotationPoint;
        this.orientations = orientations;

        if (this.configuration.isInnerReflex() || this.configuration.isInnerConvex()) {
            return;
        }

        this.rotation = calculateRotation();
        if (this.rotation != null) {
            this.area = calculateArea(this.rotation);
        }
        if (this.area <= DoubleUtil.EPS){
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
        return rotation != null;
    }

    @Override
    public double getAffectedArea() {
        return area;
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
    protected LineSegment calculateRotation() {
        var largestArea = -1.0;
        LineSegment largestRotation = null;

        for (OrientationSet.Orientation orientation : orientations) {
            direction = orientation.getDirection();

            var innerDirection = configuration.inner.getDirection();
            innerDirection.normalize();
            direction.normalize();
            if (innerDirection.isApproximately(direction) || innerDirection.isApproximately(Vector.multiply(-1, direction)))
                continue;

            var resultLine = new Line(rotationPoint, direction);

            var prevIntersections = resultLine.intersect(configuration.previous);
            if (!(prevIntersections.size() == 1 && prevIntersections.get(0) instanceof Vector)) {
                continue;
            }
            var nextIntersections = resultLine.intersect(configuration.next);
            if (!(nextIntersections.size() == 1 && nextIntersections.get(0) instanceof Vector)) {
                continue;
            }

            var newPrev = (Vector) prevIntersections.get(0);
            var newNext = (Vector) nextIntersections.get(0);
            var newRotation = new LineSegment(newPrev, newNext);
            var newArea = calculateArea(newRotation);

            if  (newArea > largestArea) {
                largestArea = newArea;
                largestRotation = newRotation;
            }
        }

        return largestRotation;
    }

    private double calculateArea(LineSegment rotation) {
        var prevTriangle = new Polygon(rotationPoint, configuration.previous.getEnd(), rotation.getStart());
        var nextTriangle = new Polygon(rotationPoint, configuration.next.getStart(), rotation.getEnd());
        return prevTriangle.areaUnsigned() + nextTriangle.areaUnsigned();
    }

}
