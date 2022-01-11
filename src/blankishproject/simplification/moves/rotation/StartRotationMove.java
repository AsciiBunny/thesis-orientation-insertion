package blankishproject.simplification.moves.rotation;

import blankishproject.OrientationSet;
import blankishproject.simplification.Configuration;

public class StartRotationMove extends RotationMove {
    public StartRotationMove(Configuration configuration, OrientationSet orientations) {
        super(configuration, configuration.inner.getStart().clone(), orientations);
    }
}
