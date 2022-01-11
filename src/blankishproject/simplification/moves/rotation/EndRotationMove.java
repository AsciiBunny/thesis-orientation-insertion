package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;

public class EndRotationMove extends RotationMove {
    public EndRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, configuration.inner.getEnd().clone(), orientations);
    }
}
