package blankishproject.deciders;

import blankishproject.edgelist.Configuration;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.moves.MoveType;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

public class MinimalPairDecider implements IDecider {
    @Override
    public List<Decision> findMoves(Polygon polygon, ConfigurationList configurations) {
        Configuration minPositive = null;
        double minPositiveArea = Double.MAX_VALUE;
        for (var configuration : configurations) {
            if (configuration.positiveNormalMove.hasValidContraction()) {
                var area = Math.abs(configuration.positiveNormalMove.getArea());
                if (area < minPositiveArea) {
                    minPositive = configuration;
                    minPositiveArea = area;
                }
            }
        }
        if (minPositive == null)
            return Collections.emptyList();

        Configuration minNegative = null;
        double minNegativeArea = Double.MAX_VALUE;
        for (var configuration : configurations) {
            if (configuration.negativeNormalMove.hasValidContraction() && !doCollide(minPositive, configuration)) {
                var area = Math.abs(configuration.negativeNormalMove.getArea());
                if (area < minNegativeArea) {
                    minNegative = configuration;
                    minNegativeArea = area;
                }
            }
        }

        if (minNegative == null)
            return Collections.emptyList();

        return List.of(new Decision(minPositive, MoveType.POSITIVE), new Decision(minNegative, MoveType.NEGATIVE));

    }

    private boolean doCollide(Configuration positive, Configuration negative) {
        return undirectedEquals(negative.inner, positive.previous)
            || undirectedEquals(negative.next, positive.previous)
            || undirectedEquals(positive.inner, negative.previous)
            || undirectedEquals(positive.next, negative.previous);
    }
}
