package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class MinimalComplementaryPairDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var bestSpecialPair = findPairMove(data);
        var bestNormalPair = findNormalPair(data);

        System.out.println(bestSpecialPair.size() > 0 ? "Using Complementary Pair" : "Using Non-complementary Pair");
        return bestSpecialPair.size() > 0 ? bestSpecialPair : bestNormalPair;
    }

    public List<Decision> findNormalPair(SimplificationData data) {
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

    private List<Decision> findPairMove(SimplificationData data) {
        var bestPositive = findSmallest(data.positivePairMoves);
        var bestNegative = findSmallest(data.negativePairMoves);
        var smallest = getSmallest(bestPositive, bestNegative);

        return smallest != null ? Collections.singletonList(new Decision(smallest.configuration, smallest, smallest.getAffectedArea(), false)) : Collections.emptyList();
    }
}
