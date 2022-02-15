package thesis.simplification;

import thesis.Util;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;

import static thesis.Util.undirectedEquals;

public class Configuration {

    private final Polygon polygon;
    public int index;
    public LineSegment previous;
    public LineSegment inner;
    public LineSegment next;

    private boolean invalidated = false;

    public Configuration(Polygon polygon, int index) {
        this.polygon = polygon;
        this.index = index;

        init();
    }

    public void init() {
        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edges::add);

        this.previous = edges.get((index + edges.size() - 1) % edges.size());
        this.inner = edges.get(index % edges.size());
        this.next = edges.get((index + 1) % edges.size());
    }

    public boolean wasInvalidated() {
        return index == -1
                || invalidated
                || DoubleUtil.close(inner.length(), 0)
                || (
                !DoubleUtil.close(previous.length(), 0)
                        && (
                        DoubleUtil.close(getStartAngle(), 0)
                                || DoubleUtil.close(getStartAngle(), Math.PI)
                                || DoubleUtil.close(getStartAngle(), 2 * Math.PI))
        );
    }

    public String invalidationReason() {
        if (index == -1) return "Invalid index";
        if (invalidated) return "Invalidated during cleanup";
        if (DoubleUtil.close(inner.length(), 0)) return "inner was reduced to length 0";
        if (DoubleUtil.close(getStartAngle(), 0) || DoubleUtil.close(getStartAngle(), Math.PI) || DoubleUtil.close(getStartAngle(), 2 * Math.PI))
            return "inner.start was made unnecessary";

        return "This should never be returned";
    }

    //region Tracks
    public Line getPreviousTrack() {
        return Util.extendLine(previous);
    }

    public Line getInnerTrack() {
        return Util.extendLine(inner);
    }

    public Line getNextTrack() {
        return Util.extendLine(next);
    }
    //endregion

    //region Inner Normals

    /**
     * Returns the outwards normal of the inner edge
     * <br> Complexity O(1)
     */
    public Vector getNormal() {
        var normal = inner.getDirection();
        normal.rotate90DegreesClockwise();
        normal.normalize();
        return normal;
    }

    /**
     * Returns the outwards normal of the inner edge
     * <br> Complexity O(1)
     */
    public Vector getOutwardsNormal() {
        return getNormal();
    }

    /**
     * Returns the inwards normal of the inner edge
     * <br> Complexity O(1)
     */
    public Vector getInwardsNormal() {
        var normal = getNormal();
        normal.scale(-1);
        return normal;
    }

    public boolean isInsidePolygon(Vector point) {
        return (inner.getEnd().getX() - inner.getStart().getX()) * (point.getY() - inner.getStart().getY()) - (inner.getEnd().getY() - inner.getStart().getY()) * (point.getX() - inner.getStart().getX()) > 0;
    }
    //endregion Inner Normals

    //region Convexity/Reflexity

    /**
     * Returns the angle of the previous edge and the inner edge
     * <br> Complexity O(1)
     */
    public double getStartAngle() {
        return previous.getDirection().computeClockwiseAngleTo(inner.getDirection());
    }

    /**
     * Returns the angle of the inner edge and the next edge
     * <br> Complexity O(1)
     */
    public double getEndAngle() {
        return inner.getDirection().computeClockwiseAngleTo(next.getDirection());
    }

    /**
     * Returns whether the start vector of the inner edge is reflex
     * <br> Complexity O(1)
     */
    public boolean isStartReflex() {
        var angle = getStartAngle();
        return angle < Math.PI;
    }

    /**
     * Returns whether the end vector of the inner edge is reflex
     * <br> Complexity O(1)
     */
    public boolean isEndReflex() {
        var angle = getEndAngle();
        return angle < Math.PI;
    }

    /**
     * Returns whether the start vector of the inner edge is convex
     * <br> Complexity O(1)
     */
    public boolean isStartConvex() {
        return !isStartReflex();
    }

    /**
     * Returns whether the end vector of the inner edge is convex
     * <br> Complexity O(1)
     */
    public boolean isEndConvex() {
        return !isEndReflex();
    }

    /**
     * Returns whether the inner edge is convex
     * <br> Complexity O(1)
     */
    public boolean isInnerConvex() {
        return isStartConvex() && isEndConvex();
    }

    /**
     * Returns whether the inner edge is reflex
     * <br> Complexity O(1)
     */
    public boolean isInnerReflex() {
        return isStartReflex() && isEndReflex();
    }
    //endregion Convexity/Reflexity

    public boolean isSpecialPairNeighbouring(Configuration other) {
        return (undirectedEquals(other.next, this.previous) && ((other.isEndReflex() && this.isStartConvex()) || (other.isEndConvex() && this.isStartReflex())))
                || (undirectedEquals(this.next, other.previous) && ((this.isEndReflex() && other.isStartConvex()) || (this.isEndConvex() && other.isStartReflex())));
    }

    public boolean isColliding(Configuration other) {
        return this == other
                || undirectedEquals(this.previous, other.next)
                || undirectedEquals(this.next, other.previous)
                || undirectedEquals(this.inner, other.next)
                || undirectedEquals(this.inner, other.previous)
                || undirectedEquals(this.inner, other.inner);
    }
}
