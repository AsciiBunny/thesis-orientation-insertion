package blankishproject.edgelist;

import blankishproject.Data;
import blankishproject.Util;
import blankishproject.moves.*;
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

    public final Data data;
    private final Polygon polygon;
    public int index;
    public LineSegment previous;
    public LineSegment inner;
    public LineSegment next;

    public PositiveNormalMove positiveNormalMove;
    public NegativeNormalMove negativeNormalMove;
    public PairNormalMove positivePairMove;
    public PairNormalMove negativePairMove;

    public Configuration(Data data, int index) {
        this.data = data;
        this.polygon = data.simplification;
        this.index = index;

        init();
    }

    public void init() {
        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edges::add);

        this.previous = edges.get((index + edges.size() - 1) % edges.size());
        this.inner = edges.get(index % edges.size());
        this.next = edges.get((index + 1) % edges.size());

        this.positiveNormalMove = new PositiveNormalMove(this, polygon);
        this.negativeNormalMove = new NegativeNormalMove(this, polygon);
    }

    public Move getMove(MoveType moveType) {
        return switch (moveType) {
            case POSITIVE -> positiveNormalMove;
            case NEGATIVE -> negativeNormalMove;
            case POSITIVE_PAIR -> positivePairMove;
            case NEGATIVE_PAIR -> negativePairMove;
            default -> throw new IllegalStateException("Unexpected value: " + moveType);
        };
    }

    public boolean hasMove(MoveType moveType) {
        var move = getMove(moveType);
        if (move == null)
            return false;
        if (!move.isValid())
            return false;
        return true;
    }

    /**
     * Apply a move, optionally apply cleanup code to make sure duplicate vertexes get removed
     * <br> Complexity O(1) | O(n) when other configurations get updated, this is not yet implemented
     */
    public List<Integer> performMove(MoveType moveType, double area, boolean cleanup) {
        var move = getMove(moveType);

        move.applyForArea(area);

        if (cleanup) {
            // TODO: Keep configurationList in sync with polygon during cleanup
            return moveCleanup();
        } else {
            return Collections.emptyList();
        }
    }

    private List<Integer> moveCleanup() {
        assert index == polygon.vertices().indexOf(inner.getStart()) : "Index (" + index + ") is inaccurate (should be " + polygon.vertices().indexOf(inner.getStart()) + "), likely not updated correctly after performing previous contraction";

        var invalid = false;
        // Cleanup
        var removed = new ArrayList<Integer>();
        // Previous got shortened into non-existence
        if (previous.getStart().isApproximately(previous.getEnd())) {
            var removeIndex = (index - 1 + polygon.vertexCount()) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == previous.getStart();
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);

            if (removeIndex < index) {
                index -= 1;
            }

            // todo pas neighbour aan
        }

        // Next got shortened into non-existence
        if (next.getStart().isApproximately(next.getEnd())) {
            var removeIndex = (index + 2) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == next.getEnd();
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);

            if (removeIndex < index) {
                index -= 1;
            }
        }

        assert index == polygon.vertices().indexOf(inner.getStart()) : "Index (" + index + ") was updated incorrectly (should be " + polygon.vertices().indexOf(inner.getStart()) + ") after removing vertices";

        // Previous got shortened into non-existence and inner is now an extension of previous' previous
        var previousAngle = inner.getDirection().computeClockwiseAngleTo(polygon.edge((index - 1) % polygon.vertexCount()).getDirection());
        if (DoubleUtil.close(previousAngle, 0) || DoubleUtil.close(previousAngle, Math.PI) || DoubleUtil.close(previousAngle, 2 * Math.PI)) {
            var removeIndex = index;
            assert polygon.vertex(removeIndex) == inner.getStart();
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);
            index -= 1;
            invalid = true;
        }

        // Next got shortened into non-existence and inner is now an extension of next's next
        var nextAngle = inner.getDirection().computeClockwiseAngleTo(polygon.edge((index + 1) % polygon.vertexCount()).getDirection());
        if (DoubleUtil.close(nextAngle, 0) || DoubleUtil.close(nextAngle, Math.PI) || DoubleUtil.close(nextAngle, 2 * Math.PI)) {
            var removeIndex = (index + 1) % polygon.vertexCount();
            assert polygon.vertex(removeIndex) == inner.getEnd();
            polygon.removeVertex(removeIndex);

            removed.add(removeIndex);
            if (removeIndex < index) {
                index -= 1;
            }
        }

        // Inner got shortened into non-existence
        if (inner.getStart().isApproximately(inner.getEnd())) {
            var removeIndex = index;
            assert (polygon.vertex(removeIndex).isApproximately(inner.getStart()));
            polygon.removeVertex(removeIndex);
            removed.add(removeIndex);
            invalid = true;
        }
        if (invalid)
            index = -1;

        assert index == polygon.vertices().indexOf(inner.getStart()) : "Index (" + index + ") is inaccurate (should be " + polygon.vertices().indexOf(inner.getStart()) + "), likely not updated correctly after performing previous contraction";

        return removed;
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
