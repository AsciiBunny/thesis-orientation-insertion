package thesis;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Line;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    public static boolean undirectedEquals(LineSegment a, LineSegment b) {
        return (a.getStart().isApproximately(b.getStart()) && a.getEnd().isApproximately(b.getEnd())) || (a.getStart().isApproximately(b.getEnd()) && a.getEnd().isApproximately(b.getStart()));
    }

    public static Line extendLine(LineSegment segment) {
        return Line.byThroughpoints(segment.getStart(), segment.getEnd());
    }

    public static Polygon finishPolyLine(PolyLine line) {
        var vertices = new ArrayList<>(line.vertices());
        return new Polygon(vertices);
    }


    public static List<Polygon> calculateSymmetricDifference(Polygon a, Polygon b) {
        Geometry faceA = polygonToGeometry(a);
        Geometry faceB = polygonToGeometry(b);

        var intersection = faceA.difference(faceB);
        var difference = new ArrayList<Polygon>();
        for (int i = 0; i < intersection.getNumGeometries(); i++) {
            difference.add(geometryToPolygon(intersection.getGeometryN(i)));
        }

        return difference;
    }

    private static final GeometryFactory geofac = new GeometryFactory();
    public static Geometry polygonToGeometry(Polygon polygon) {
        var coordinates = new ArrayList<Coordinate>(polygon.vertices().size() + 1);
        Vector first = null;
        for (int i = 0; i < polygon.vertices().size(); i++) {
            var v = polygon.vertices().get(i);
            coordinates.add(new Coordinate(v.getX(), v.getY()));
            if (first == null) {
                first = v;
            }
        }
        coordinates.add(new Coordinate(first.getX(), first.getY()));
        return geofac.createPolygon(coordinates.toArray(new Coordinate[]{}));
    }

    public static Polygon geometryToPolygon(Geometry geometry) {
        var polygon = new Polygon();
        var coordinates = geometry.getCoordinates();
        for (var coordinate: coordinates) {
            polygon.addVertex(new Vector(coordinate.x, coordinate.y));
        }
        return polygon;
    }

    public static int countTrue(boolean... booleans) {
        var count = 0;
        for (boolean bool : booleans) {
            if (bool)
                count ++;
        }
        return count;
    }
}
