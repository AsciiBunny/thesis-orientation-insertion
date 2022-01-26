package blankishproject.simplification.deciders;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.NegativeNormalMove;
import blankishproject.simplification.moves.moving.PositiveNormalMove;
import blankishproject.simplification.moves.rotation.RotationMove;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

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

        if (DoubleUtil.close(minNegativeArea, minPositiveArea)) { //(minNegativeArea == minPositiveArea)
            return List.of(new Decision(minPositive.configuration, minPositive), new Decision(minNegative.configuration, minNegative));
        } else if (minNegativeArea < minPositiveArea) {
            return List.of(new Decision(minPositive.configuration, minPositive, minNegativeArea), new Decision(minNegative.configuration, minNegative));
        } else { //(minNegativeArea > minPositiveArea)
            return List.of(new Decision(minNegative.configuration, minNegative, minPositiveArea), new Decision(minPositive.configuration, minPositive));
        }

    }

    private List<Decision> findPairMove(SimplificationData data) {
        var bestPositive = findSmallest(data.positivePairMoves);
        var bestNegative = findSmallest(data.negativePairMoves);
        var smallest = getSmallest(bestPositive, bestNegative);

        return smallest != null ? Collections.singletonList(new Decision(smallest.configuration, smallest, smallest.getAffectedArea(), false)) : Collections.emptyList();
    }

    private List<Decision> findRotation(SimplificationData data) {
        var min = findSmallest(data.middleRotationMoves);

        if (min == null)
            return Collections.emptyList();

        if (!data.compensateSingleMoves)
            return Collections.singletonList(new Decision(min.configuration, min));

        var compensation = findCompensator(data, min, min.configuration.index);
        return List.of(new Decision(min.configuration, min), compensation);
    }

    private boolean doCollide(Configuration positive, Configuration negative) {
        return undirectedEquals(negative.inner, positive.previous)
                || undirectedEquals(negative.next, positive.previous)
                || undirectedEquals(negative.inner, positive.inner)
                || undirectedEquals(positive.inner, negative.previous)
                || undirectedEquals(positive.next, negative.previous);
    }
}
