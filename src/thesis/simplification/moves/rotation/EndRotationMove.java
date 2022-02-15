package thesis.simplification.moves.rotation;

import thesis.OrientationSet;
import thesis.simplification.Configuration;
import thesis.simplification.SimplificationData;
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
