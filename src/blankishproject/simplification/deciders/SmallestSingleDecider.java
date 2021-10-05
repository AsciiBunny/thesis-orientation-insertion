package blankishproject.simplification.deciders;

import blankishproject.simplification.moves.MoveType;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class SmallestSingleDecider implements IDecider{

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var configurations = data.configurations;
        Configuration min = null;
        double minArea = Double.MAX_VALUE;
        MoveType type = MoveType.NONE;
        for (var configuration : configurations) {
            if (configuration.positiveNormalMove.hasValidContraction()) {
                var positiveArea = Math.abs(configuration.positiveNormalMove.getArea());
                if (positiveArea < minArea) {
                    min = configuration;
                    minArea = positiveArea;
                    type = MoveType.POSITIVE;
                }
            }

            if (configuration.negativeNormalMove.hasValidContraction()) {
                var negativeArea = Math.abs(configuration.negativeNormalMove.getArea());
                if (negativeArea < minArea) {
                    min = configuration;
                    minArea = negativeArea;
                    type = MoveType.NEGATIVE;
                }
            }
        }

        return type != MoveType.NONE ? Collections.singletonList(new Decision(min, type)) : Collections.emptyList();
    }
}
