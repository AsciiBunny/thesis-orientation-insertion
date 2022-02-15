package thesis.simplification.deciders;

import thesis.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class SmallestSingleDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var smallestPositive = findSmallest(data.positiveMoves);
        var smallestNegative = findSmallest(data.negativeMoves);
        var smallest = getSmallest(smallestNegative, smallestPositive);

        return smallest != null ? Collections.singletonList(new Decision(smallest.configuration, smallest)) : Collections.emptyList();
    }
}
