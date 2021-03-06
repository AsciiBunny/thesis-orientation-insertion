package thesis.simplification.deciders;

import thesis.simplification.SimplificationData;
import thesis.simplification.moves.rotation.RotationMove;

import java.util.Collections;
import java.util.List;

public class OnlyCompensatingRotationDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        RotationMove min = findSmallest(data.compensatingRotationMoves);

        if (min == null)
            return Collections.emptyList();

        return Collections.singletonList(new Decision(min.configuration, min));
    }
}
