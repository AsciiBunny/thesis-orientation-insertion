package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import nl.tue.geometrycore.geometry.Vector;

public class MiddleRotationMove extends RotationMove {
    public MiddleRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        return configuration.inner.getPointAlongPerimeter(0.5);
    }
}
