package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class MinimalPairDecider extends IDecider {
    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var minPositive = findSmallest(data.positiveMoves);
        var minNegative = findSmallest(data.negativeMoves);
        var min = getSmallest(minPositive, minNegative);

        if (min == null)
            return Collections.emptyList();

        var compensation = findCompensator(data, min, min.configuration.index);

        if (compensation == null)
            return Collections.emptyList();

        return List.of(new Decision(min.configuration, min), compensation);
    }
}
