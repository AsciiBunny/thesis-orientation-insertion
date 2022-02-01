package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import nl.tue.geometrycore.geometry.Vector;

public class StartRotationMove extends RotationMove {
    public StartRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        return configuration.inner.getStart().clone();
    }
}
