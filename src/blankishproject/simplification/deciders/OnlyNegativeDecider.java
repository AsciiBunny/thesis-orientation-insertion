package blankishproject.simplification.deciders;

import blankishproject.simplification.moves.MoveType;
import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;

import java.util.Collections;
import java.util.List;

public class OnlyNegativeDecider implements IDecider{

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var configurations = data.configurations;
        Configuration min = null;
        double minArea = Double.MAX_VALUE;
        for (var configuration : configurations) {
            if (configuration.negativeNormalMove.hasValidContraction()) {
                var area = Math.abs(configuration.negativeNormalMove.getArea());
                if (area < minArea) {
                    min = configuration;
                    minArea = area;
                }
            }
        }

        return min != null ? Collections.singletonList(new Decision(min, MoveType.NEGATIVE)) : Collections.emptyList();
    }
}
