package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;

import java.util.List;
import java.util.Map;

public interface IDecider {

    Map<String, IDecider> deciders = Map.ofEntries(
        Map.entry("0. Only Positive", new OnlyPositiveDecider()),
        Map.entry("1. Only Negative", new OnlyNegativeDecider()),
        Map.entry("2. Smallest Single", new SmallestSingleDecider()),
        Map.entry("3. Minimal Pair", new MinimalPairDecider()),
        Map.entry("4. Minimal Complementary Pair", new MinimalComplementaryPairDecider())
    );

    List<Decision> findMoves(SimplificationData data);

}

