package thesis.simplification.moves.rotation;

import thesis.OrientationSet;
import thesis.simplification.Configuration;
import thesis.simplification.SimplificationData;
import nl.tue.geometrycore.geometry.Vector;

public class MiddleRotationMove extends RotationMove {
    public MiddleRotationMove(SimplificationData data, Configuration configuration, OrientationSet orientations) {
        super(data, configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        return configuration.inner.getPointAlongPerimeter(0.5);
    }
}
