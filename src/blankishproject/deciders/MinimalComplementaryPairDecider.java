package blankishproject.deciders;

import blankishproject.edgelist.Configuration;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.moves.MoveType;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

public class MinimalComplementaryPairDecider implements IDecider {

    @Override
    public List<Decision> findMoves(Polygon polygon, ConfigurationList configurations) {
        var bestSpecialPair = findPairMove(configurations);
        var bestNormalPair = findNormalPair(polygon, configurations);

        System.out.println("bestSpecialPair.size() = " + bestSpecialPair.size());
        return bestSpecialPair.size() > 0 ? bestSpecialPair : bestNormalPair;
    }

    public List<Decision> findNormalPair(Polygon polygon, ConfigurationList configurations) {
        Configuration minPositive = null;
        double minPositiveArea = Double.MAX_VALUE;
        for (var configuration : configurations) {
            if (configuration.positiveNormalMove.hasValidContraction()) {
                var area = Math.abs(configuration.positiveNormalMove.getArea());
                if (area < minPositiveArea + DoubleUtil.EPS) {
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
                if (area < minNegativeArea + DoubleUtil.EPS) {
                    minNegative = configuration;
                    minNegativeArea = area;
                }
            }
        }

        if (minNegative == null)
            return Collections.emptyList();

        if (DoubleUtil.close(minNegativeArea, minPositiveArea)) { //(minNegativeArea == minPositiveArea)
            return List.of(new Decision(minPositive, MoveType.POSITIVE), new Decision(minNegative, MoveType.NEGATIVE));
        } else if (minNegativeArea < minPositiveArea) {
            return List.of(new Decision(minPositive, MoveType.POSITIVE, minNegativeArea), new Decision(minNegative, MoveType.NEGATIVE));
        } else { //(minNegativeArea > minPositiveArea)
            return List.of(new Decision(minNegative, MoveType.NEGATIVE, minPositiveArea), new Decision(minPositive, MoveType.POSITIVE));
        }

    }

    private List<Decision> findPairMove(ConfigurationList configurations) {
        var bestArea = Double.MAX_VALUE;
        List<Decision> best = Collections.emptyList();

        for (int i = 0; i < configurations.size(); i++) {
            var configuration = configurations.get(i);

            if (configuration.hasMove(MoveType.POSITIVE_PAIR)) {
                var move = configuration.getMove(MoveType.POSITIVE_PAIR);
                if (move.getAffectedArea() < bestArea) {
                    best = Collections.singletonList(new Decision(configuration, MoveType.POSITIVE_PAIR, move.getAffectedArea(), false));
                    bestArea = move.getAffectedArea();
                }
            }

            if (configuration.hasMove(MoveType.NEGATIVE_PAIR)) {
                var move = configuration.getMove(MoveType.NEGATIVE_PAIR);
                if (move.getAffectedArea() < bestArea) {
                    best = Collections.singletonList(new Decision(configuration, MoveType.NEGATIVE_PAIR, move.getAffectedArea(), false));
                    bestArea = move.getAffectedArea();
                }
            }

        }

        return best;
    }

    private boolean doCollide(Configuration positive, Configuration negative) {
        return undirectedEquals(negative.inner, positive.previous)
                || undirectedEquals(negative.next, positive.previous)
                || undirectedEquals(negative.inner, positive.inner)
                || undirectedEquals(positive.inner, negative.previous)
                || undirectedEquals(positive.next, negative.previous);
    }
}
