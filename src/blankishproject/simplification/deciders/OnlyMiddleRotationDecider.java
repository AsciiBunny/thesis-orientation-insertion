package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.rotation.RotationMove;

import java.util.Collections;
import java.util.List;

public class OnlyMiddleRotationDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        RotationMove min = findSmallest(data.middleRotationMoves);

        if (min == null)
            return Collections.emptyList();

        if (!data.compensateSingleMoves)
            return Collections.singletonList(new Decision(min.configuration, min));

        var compensation = findCompensator(data, min, min.configuration.index);
        return List.of(new Decision(min.configuration, min), compensation);
    }
}
