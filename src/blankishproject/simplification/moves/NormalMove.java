package blankishproject.simplification.moves;

import blankishproject.Util;
import blankishproject.simplification.Configuration;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static blankishproject.Util.extendLine;
import static blankishproject.Util.undirectedEquals;

public abstract class NormalMove extends Move {

    public final Configuration configuration;
    protected final LineSegment contraction;
    protected final Vector direction;
    protected double distance;

    protected List<Vector> blockingVectors;

    public NormalMove(Configuration configuration, Polygon polygon) {
        this.configuration = configuration;
        this.distance = calculateDistance();
        this.direction = calculateDirection();
        this.contraction = calculateContraction();

        this.blockingVectors = calculateBlockingVectors(polygon);
    }

    @Override
    public boolean isValid() {
        return hasValidContraction();
    }

    @Override
    public double getAffectedArea() {
        return getArea();
    }

    public void recalculate() {
        this.distance = calculateDistance();
    }

    //region getters
    public LineSegment getContraction() {
        return contraction;
    }

    public boolean hasContraction() {
        return contraction != null;
    }

    public boolean hasValidContraction() {
        return hasContraction() && getBlockingNumber() == 0;
    }

    public Vector getDirection() {
        return direction;
    }

    public double getDistance() {
        return distance;
    }

    public double getArea() {
        return 0.5 * distance * (contraction.length() + configuration.inner.length());
    }

    public int getBlockingNumber() {
        return blockingVectors.size();
    }

    public List<Vector> getBlockingVectors() {
        return blockingVectors;
    }
    //endregion

    public double getAreaForDistance(double moveDistance) {
        if (moveDistance < 0 || moveDistance > getDistance() + DoubleUtil.EPS)
            throw new IllegalArgumentException("moveDistance out of possible move bounds [0, " + getDistance() + "]: " + moveDistance);

        if (DoubleUtil.close(moveDistance, distance))
            return getArea();

        var move = getForDistance(moveDistance);
        return getAreaForMove(move);
    }

    public double getAreaForMove(LineSegment move) {
        var polygon = new Polygon(configuration.inner.getStart(), configuration.inner.getEnd(), move.getEnd(), move.getStart());
        return polygon.areaUnsigned();
    }

    public LineSegment getForArea(double removeArea) {
        if (DoubleUtil.close(removeArea, 0))
            return configuration.inner;
        if (DoubleUtil.close(removeArea, getArea()))
            return contraction;
        if (removeArea > getArea() || removeArea < 0)
            throw new IllegalArgumentException("removeArea out of possible move bounds [0, " + getArea() + "]: " + removeArea);

        var inner = configuration.inner;
        var innerLength = inner.length();
        var outerLength = contraction.length();

        assert innerLength > 0 : "Can't move invalid edge: " + inner;

        var midLength = Math.sqrt(2 * outerLength * removeArea + innerLength * (innerLength * distance - 2 * removeArea)) / Math.sqrt(distance);
        var removeDistance = (2 * removeArea) / (midLength + innerLength);

        assert removeDistance > 0 && removeDistance < distance : "Invalid distance calculated: " + removeDistance;

        var mid = getForDistance(removeDistance);

        assert DoubleUtil.close(midLength, mid.length()) : "Invalid midLength: " + midLength + " != " + mid.length()
                + "\n       " + configuration.previous.getStart() + configuration.previous.getEnd() + configuration.next.getStart() + configuration.next.getEnd()
                + "\n       " + configuration.previous.length() + " " + configuration.next.length()
                + "\n       " + contraction
                + "\n       " + configuration.wasInvalidated()
                + "\n       " + configuration.inner;

        return mid;
    }

    public LineSegment getForDistance(double moveDistance) {
        var removeVector = Vector.multiply(moveDistance, direction);
        var mid = configuration.inner.clone();
        mid.translate(removeVector);
        var midLine = Util.extendLine(mid);

        mid.setStart((Vector) Util.extendLine(configuration.previous).intersect(midLine).get(0));
        mid.setEnd((Vector) Util.extendLine(configuration.next).intersect(midLine).get(0));

        return mid;
    }

    public void applyForArea(double removeArea) {
        var move = DoubleUtil.close(removeArea, this.getArea()) ? contraction : getForArea(removeArea);
        //var areaBefore = getArea();
        apply(move);
        //assert DoubleUtil.close(areaBefore - removeArea, getArea()) : "Invalid area: " + areaBefore + " - " + removeArea + " != " + getArea();
    }

    public void applyForDistance(double distance) {
        var move = DoubleUtil.close(distance, this.getDistance()) ? contraction : getForDistance(distance);
        apply(move);
    }

    private void apply(LineSegment move) {
        var inner = configuration.inner;
        inner.getStart().set(move.getStart());
        inner.getEnd().set(move.getEnd());

        this.distance = calculateDistance();
    }

    public void apply() {
        apply(contraction);
    }


    //region initialization calculations
    protected abstract LineSegment calculateContraction();

    protected abstract Vector calculateDirection();

    protected abstract double calculateDistance();

    /**
     * Calculates the new inner edge for this contraction, if it exists.
     * <br> Complexity O(1)
     */
    protected LineSegment calculateNormalContraction(Vector normal) {
        var contraction = configuration.inner.clone();
        contraction.translate(normal);

        var contractionLine = extendLine(contraction);

        var previousTrack = configuration.getPreviousTrack();
        var nextTrack = configuration.getNextTrack();

        var previousIntersection = contractionLine.intersect(previousTrack);
        var nextIntersection = contractionLine.intersect(nextTrack);

        if (DoubleUtil.close(contraction.length(), 0))
            System.out.println("contraction = " + contraction + "[" + contraction.length() + "]");

        if (previousIntersection.size() == 0) {
            // TODO: Investigate case
//            System.out.println("Unexpected Geometry found for Contraction-Previous intersection:");
//            System.out.println("[No intersection found]");
//            System.out.println("previous = " + configuration.previous + " : " + configuration.previous.length());
//            System.out.println("inner = " + configuration.inner + " : " + configuration.inner.length());
//            System.out.println("next = " + configuration.next + " : " + configuration.next.length());
//            System.out.println("angle = " + configuration.getStartAngle() / (Math.PI * 2) * 360);
//            System.out.println("invalidated = " + configuration.wasInvalidated());
            //assert false;
            return null;
        } else if (previousIntersection.get(0) instanceof Line) {
            contraction.setStart(configuration.previous.getStart().clone());
        } else if (previousIntersection.get(0) instanceof Vector) {
            contraction.setStart((Vector) previousIntersection.get(0).clone());
        } else {
            System.out.println("Unexpected Geometry found for Contraction-Previous intersection: ");
            System.out.println(previousIntersection);
            return null;
        }

        if (nextIntersection.size() == 0) {
            // TODO: Investigate case
//            System.out.println("Unexpected Geometry found for Contraction-Next intersection:");
//            System.out.println("[No intersection found]");
//            System.out.println("previous = " + configuration.previous + " : " + configuration.previous.length());
//            System.out.println("inner = " + configuration.inner + " : " + configuration.inner.length());
//            System.out.println("next = " + configuration.next + " : " + configuration.next.length());
//            System.out.println("endAngle = " + configuration.getEndAngle() / (Math.PI * 2) * 360);
//            System.out.println("invalidated = " + configuration.wasInvalidated());
            //assert false;
            return null;
        } else if (nextIntersection.get(0) instanceof Line) {
            contraction.setEnd(configuration.next.getEnd().clone());
        } else if (nextIntersection.get(0) instanceof Vector) {
            contraction.setEnd((Vector) nextIntersection.get(0).clone());
        } else {
            System.out.println("Unexpected Geometry found for Contraction-Next intersection:");
            System.out.println(nextIntersection);
            return null;
        }

//        if (DoubleUtil.close(contraction.length(), 0))
//            return contraction;

        // Determine if edge case where previous and next cross
        var previousNextIntersection = previousTrack.intersect(nextTrack);
        if (previousNextIntersection.size() == 0)
            return contraction;


        if (!(previousNextIntersection.get(0) instanceof Vector)) {
            System.out.println("Unexpected Geometry found for Previous-Next intersection:");
            System.out.println(previousNextIntersection);
            return null;
        }

        var intersection = (Vector) previousNextIntersection.get(0);
        var closest = extendLine(configuration.inner).closestPoint(intersection);
        var intersectionDirection = Vector.subtract(intersection, closest);
        var intersectionDistance = intersectionDirection.length();
        intersectionDirection.normalize();
        var normalizedNormal = normal.clone();
        normalizedNormal.normalize();
        if (intersectionDirection.isApproximately(normalizedNormal) && (intersectionDistance < distance || DoubleUtil.close(intersectionDistance, distance))) {
            contraction.setStart(intersection.clone());
            contraction.setEnd(intersection.clone());

            //todo: dirty edit -> move checks to distance calculation to prevent edge case from happening
            distance = intersectionDistance;
        }

        return contraction;
    }

    /**
     * Find all blocking vectors for this contraction.
     * <br> Complexity O(n)
     */
    protected List<Vector> calculateBlockingVectors(Polygon polygon) {
        if (contraction == null)
            return new ArrayList<>();

        var previous = configuration.previous;
        var inner = configuration.inner;
        var next = configuration.next;

        var boundary = Arrays.asList(previous.getStart(), inner.getStart(), next.getStart(), next.getEnd());
        var contractionArea = new Polygon(inner.getStart(), inner.getEnd(), contraction.getEnd(), contraction.getStart());

        //var vertices = polygon.vertices().stream().filter(vector -> (contractionArea.onBoundary(vector) || contractionArea.contains(vector))&& !boundary.contains(vector)).collect(Collectors.toList());
        var vertices = new ArrayList<Vector>();
        polygon.edges().forEach(edge -> {
            if (undirectedEquals(edge, previous) || undirectedEquals(edge, inner) || undirectedEquals(edge, next))
                return;

            var intersection = contractionArea.intersect(edge);
            //noinspection rawtypes
            for (BaseGeometry geo : intersection) {
                if (geo instanceof Vector) {
                    vertices.add((Vector) geo);
                } else if (geo instanceof LineSegment) {
                    vertices.add(((LineSegment) geo).getStart());
                    vertices.add(((LineSegment) geo).getEnd());
                }
            }
        });

        return vertices.stream().filter(vector -> boundary.stream().noneMatch(bound -> bound.isApproximately(vector))).collect(Collectors.toList());
    }
    //endregion

    public void updateBlockingVectors(List<Vector> removed) {
        if (!this.hasContraction()) return;

        blockingVectors.removeAll(removed);

        var contractionArea = new Polygon(configuration.inner.getStart(), configuration.inner.getEnd(), contraction.getEnd(), contraction.getStart());
        var before = getBlockingNumber();
        blockingVectors = blockingVectors.stream().filter(contractionArea::contains).collect(Collectors.toList());

        if (getBlockingNumber() != before)
            System.out.println("Removed " + (before - getBlockingNumber()) + " blocking vertices");
    }
}
