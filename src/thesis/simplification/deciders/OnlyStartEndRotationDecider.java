package thesis.simplification.deciders;

import thesis.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class OnlyStartEndRotationDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var smallestStart = findSmallest(data.startRotationMoves);
        var smallestEnd = findSmallest(data.endRotationMoves);
        var min = getSmallest(smallestStart, smallestEnd);

        if (min == null)
            return Collections.emptyList();

        if (!data.compensateSingleMoves)
            return Collections.singletonList(new Decision(min.configuration, min));

        var compensation = findCompensator(data, min, min.configuration.index);
        return List.of(new Decision(min.configuration, min), compensation);
    }
}
