package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;
import nl.tue.geometrycore.geometry.Vector;

public class EndRotationMove extends RotationMove {
    public EndRotationMove(SimplificationData data, Configuration configuration, OrientationSet orientations) {
        super(data, configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        return configuration.inner.getEnd().clone();
    }
}
