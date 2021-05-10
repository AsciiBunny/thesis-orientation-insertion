package blankishproject;

import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryConvertable;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;

import java.util.ArrayList;

public class GeometryList<T extends BaseGeometry<T>> implements GeometryConvertable<GeometryGroup<T>> {

    private final ArrayList<T> geometries = new ArrayList<>();

    public void add(T geometry) {
        geometries.add(geometry);
    }

    @Override
    public GeometryGroup<T> toGeometry() {
        return new GeometryGroup<T>(geometries);
    }
}
