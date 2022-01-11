package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;

public class MiddleRotationMove extends RotationMove {
    public MiddleRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, configuration.inner.getPointAlongPerimeter(0.5), orientations);
    }
}
