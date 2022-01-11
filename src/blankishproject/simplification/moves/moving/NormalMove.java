package blankishproject.simplification.moves.moving;

import blankishproject.Util;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.moves.Move;
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

    protected List<LineSegment> blockingEdges;

    public NormalMove(Configuration configuration, Polygon polygon) {
        this.configuration = configuration;
        this.distance = calculateDistance();
        this.direction = calculateDirection();
        this.contraction = calculateContraction();

        this.blockingEdges = calculateBlockingVectors(polygon, contraction);
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
        return blockingEdges.size();
    }

    public List<LineSegment> getBlockingEdges() {
        return blockingEdges;
    }

    public List<Vector> getBoundary() {
        return Arrays.asList(configuration.previous.getStart(),
                configuration.inner.getStart(),
                configuration.next.getStart(),
                configuration.next.getEnd());
    }

    public Polygon getContractionArea(LineSegment outer) {
        var inner = configuration.inner;
        return new Polygon(inner.getStart(), inner.getEnd(), outer.getEnd(), outer.getStart());
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
    protected List<LineSegment> calculateBlockingVectors(Polygon polygon, LineSegment outer) {
        if (outer == null)
            return new ArrayList<>();

        var boundary = getBoundary();
        var contractionArea = getContractionArea(outer);

        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edge -> {
            if (isBlocking(contractionArea, boundary, edge))
                edges.add(edge);
        });

        return edges;
    }
    //endregion initialization calculations

    public boolean isBlocking(Polygon contractionArea, List<Vector> boundary, LineSegment edge) {
        if (undirectedEquals(edge, configuration.previous) || undirectedEquals(edge, configuration.inner) || undirectedEquals(edge, configuration.next))
            return false;

        var intersection = contractionArea.intersect(edge);

        for (var geo : intersection) {
            if (geo instanceof Vector) {
                if (boundary.stream().noneMatch(bound -> bound.isApproximately((Vector) geo)))
                    return true;
            } else if (geo instanceof LineSegment) {
                return true;
            }
        }
        return false;
    }

    public void updateBlockingVectors(List<LineSegment> removed, List<LineSegment> changed) {
        if (!this.hasContraction()) return;

        var boundary = getBoundary();
        var contractionArea = getContractionArea(contraction);

        blockingEdges = blockingEdges.stream().filter(edge -> {
            if (removed.stream().anyMatch(removedEdge -> undirectedEquals(removedEdge, edge)))
                return false;

            return isBlocking(contractionArea, boundary, edge);
        }).collect(Collectors.toList());

        changed.forEach(changedEdge -> {
            if (isBlocking(contractionArea, boundary, changedEdge))
                blockingEdges.add(changedEdge);
        });
    }
}
