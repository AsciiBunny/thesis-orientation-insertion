package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.NegativeNormalMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OnlyNegativeDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var min = findSmallest(data.negativeMoves);

        if (min == null)
            return Collections.emptyList();

        if (!data.compensateSingleMoves)
            return Collections.singletonList(new Decision(min.configuration, min));

        var compensation = findCompensator(data, min, min.configuration.index);
        return List.of(new Decision(min.configuration, min), compensation);
    }
}
