package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.NormalMove;

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
