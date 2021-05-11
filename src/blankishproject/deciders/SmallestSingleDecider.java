package blankishproject.deciders;

import blankishproject.edgelist.Configuration;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.moves.MoveType;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.Collections;
import java.util.List;

public class SmallestSingleDecider implements IDecider{

    @Override
    public List<Decision> findMoves(Polygon polygon, ConfigurationList configurations) {
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
