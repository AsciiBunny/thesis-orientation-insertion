package blankishproject.simplification.deciders;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.NegativeNormalMove;
import blankishproject.simplification.moves.moving.PositiveNormalMove;

import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

public class MinimalPairDecider implements IDecider {
    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var positiveMoves = data.positiveMoves;
        PositiveNormalMove minPositive = null;
        double minPositiveArea = Double.MAX_VALUE;
        for (var move : positiveMoves) {
            if (move.hasValidContraction()) {
                var area = Math.abs(move.getArea());
                if (area < minPositiveArea) {
                    minPositive = move;
                    minPositiveArea = area;
                }
            }
        }
        if (minPositive == null)
            return Collections.emptyList();

        var negativeMoves = data.negativeMoves;
        NegativeNormalMove minNegative = null;
        double minNegativeArea = Double.MAX_VALUE;
        for (var move : negativeMoves) {
            if (move.hasValidContraction() && !doCollide(minPositive.configuration, move.configuration)) {
                var area = Math.abs(move.getArea());
                if (area < minNegativeArea) {
                    minNegative = move;
                    minNegativeArea = area;
                }
            }
        }

        if (minNegative == null)
            return Collections.emptyList();

        return List.of(new Decision(minPositive.configuration, minPositive), new Decision(minNegative.configuration, minNegative));
    }

    private boolean doCollide(Configuration positive, Configuration negative) {
        return undirectedEquals(negative.inner, positive.previous)
            || undirectedEquals(negative.next, positive.previous)
            || undirectedEquals(positive.inner, negative.previous)
            || undirectedEquals(positive.next, negative.previous);
    }
}
