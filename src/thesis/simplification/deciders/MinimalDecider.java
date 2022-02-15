package thesis.simplification.deciders;

import thesis.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class MinimalDecider extends IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var bestRotation = findRotation(data);
        if (bestRotation.size() > 0) {
            System.out.println("Using Rotation");
            return bestRotation;
        }

        var bestSpecialPair = findPairMove(data);
        if (bestSpecialPair.size() > 0) {
            System.out.println("Using Complementary Pair");
            return bestSpecialPair;
        }

        var bestNormalPair = findNormalPair(data);
        System.out.println("Using Non-complementary Pair");
        return bestNormalPair;
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

    private List<Decision> findRotation(SimplificationData data) {
        var min = findSmallest(data.compensatingRotationMoves);

        if (min == null)
            return Collections.emptyList();

        return List.of(new Decision(min.configuration, min));
    }
}
