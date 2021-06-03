package blankishproject;

import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;

public class Util {

    public static boolean undirectedEquals(LineSegment a, LineSegment b) {
        return (a.getStart().isApproximately(b.getStart()) && a.getEnd().isApproximately(b.getEnd())) || (a.getStart().isApproximately(b.getEnd()) && a.getEnd().isApproximately(b.getStart()));
    }

    public static Line extendLine(LineSegment segment) {
        return Line.byThroughpoints(segment.getStart(), segment.getEnd());
    }

    public static Polygon finishPolyLine(PolyLine line) {
        return new Polygon(line.vertices());
    }
}
