package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;
import nl.tue.geometrycore.geometry.Vector;

public class EndRotationMove extends RotationMove {
    public EndRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, orientations);
    }

    @Override
    protected Vector getRotationPoint(OrientationSet.Orientation orientation) {
        return configuration.inner.getEnd().clone();
    }
}
