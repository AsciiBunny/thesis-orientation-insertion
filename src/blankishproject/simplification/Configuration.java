package blankishproject.simplification;

import blankishproject.Util;
import blankishproject.simplification.moves.*;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

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

    public List<Integer> moveCleanup() {
        assert index == polygon.vertices().indexOf(inner.getStart()) : "Index (" + index + ") is inaccurate (should be " + polygon.vertices().indexOf(inner.getStart()) + "), likely not updated correctly after performing previous contraction";

        var adjustedIndex = index;
        // Cleanup
        var removed = new ArrayList<Integer>();
        // Previous got shortened into non-existence
        if (previous.getStart().isApproximately(previous.getEnd())) {
            var removeIndex = (adjustedIndex - 1 + polygon.vertexCount()) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == previous.getStart() : "Index (" + removeIndex + ") is inaccurate (should be " + polygon.vertices().indexOf(previous.getStart()) + ")";
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);

            if (removeIndex < adjustedIndex) {
                adjustedIndex -= 1;
            }
        }

        // Next got shortened into non-existence
        if (next.getStart().isApproximately(next.getEnd())) {
            var removeIndex = (adjustedIndex + 1) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == next.getStart();
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);

            if (removeIndex < adjustedIndex) {
                adjustedIndex -= 1;
            }
        }

        assert adjustedIndex == polygon.vertices().indexOf(inner.getStart()) : "Index (" + adjustedIndex + ") was updated incorrectly (should be " + polygon.vertices().indexOf(inner.getStart()) + ") after removing vertices";

        // Previous got shortened into non-existence and inner is now an extension of previous' previous
        var previousAngle = inner.getDirection().computeClockwiseAngleTo(polygon.edge((adjustedIndex - 1) % polygon.vertexCount()).getDirection());
        if (DoubleUtil.close(previousAngle, 0) || DoubleUtil.close(previousAngle, Math.PI) || DoubleUtil.close(previousAngle, 2 * Math.PI)) {
            var removeIndex = adjustedIndex;
            assert polygon.vertex(removeIndex) == inner.getStart();
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);
            adjustedIndex -= 1;
            invalidated = true;
        }

        // Next got shortened into non-existence and inner is now an extension of next's next
        var nextAngle = inner.getDirection().computeClockwiseAngleTo(polygon.edge((adjustedIndex + 1) % polygon.vertexCount()).getDirection());
        if (DoubleUtil.close(nextAngle, 0) || DoubleUtil.close(nextAngle, Math.PI) || DoubleUtil.close(nextAngle, 2 * Math.PI)) {
            var removeIndex = (adjustedIndex + 1) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == inner.getEnd() || polygon.vertex(removeIndex) == next.getEnd();
            polygon.removeVertex(removeIndex);

            removed.add(removeIndex + 1);
            if (removeIndex < adjustedIndex) {
                adjustedIndex -= 1;
            }
        }

        // Inner got shortened into non-existence
        if (inner.getStart().isApproximately(inner.getEnd())) {
            var removeIndex = adjustedIndex;
            assert (polygon.vertex(removeIndex).isApproximately(inner.getStart()));
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);
            invalidated = true;
        }

        if (invalidated)
            adjustedIndex = -1;
        assert adjustedIndex == polygon.vertices().indexOf(inner.getStart()) : "Index (" + adjustedIndex + ") is inaccurate (should be " + polygon.vertices().indexOf(inner.getStart()) + "), likely not updated correctly after performing previous contraction";

        removed.sort(Collections.reverseOrder());
        return removed;
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
     * Returns the inwards normal of the inner edge
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
}
