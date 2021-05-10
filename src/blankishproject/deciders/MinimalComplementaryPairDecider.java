package blankishproject.deciders;

import blankishproject.edgelist.Configuration;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.moves.MoveType;
import blankishproject.moves.NormalMove;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static blankishproject.Util.undirectedEquals;

public class MinimalComplementaryPairDecider implements IDecider {

    @Override
    public List<Decision> findContraction(Polygon polygon, ConfigurationList configurations) {
        var bestSpecialPair = findSpecialPair(polygon, configurations);
        var bestNormalPair = findNormalPair(polygon, configurations);

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

    private List<Decision> findSpecialPair(Polygon polygon, ConfigurationList configurations) {
        var bestArea = Double.MAX_VALUE;
        List<Decision> best = Collections.emptyList();

        for (int i = 0; i < configurations.size(); i++) {
            var a = configurations.get((i - 2 + configurations.size()) % configurations.size());
            var b = configurations.get(i);

            if (!areNeighbouring(a, b))
                continue;

            NormalMove move1 = null;
            NormalMove move2 = null;

            if (a.positiveNormalMove.hasValidContraction() && b.negativeNormalMove.hasValidContraction()) {
                move1 = a.positiveNormalMove;
                move2 = b.negativeNormalMove;
                var area = calculateSpecialPairArea(a, b, move1, move2);
                if (area < bestArea) {
                    var decision1 = new Decision(a, MoveType.POSITIVE, area, true);
                    var decision2 = new Decision(b, MoveType.NEGATIVE, area, true);
                    best = List.of(decision1, decision2);
                }
            }

            if (a.negativeNormalMove.hasValidContraction() && b.positiveNormalMove.hasValidContraction()) {
                move1 = a.negativeNormalMove;
                move2 = b.positiveNormalMove;
                var area = calculateSpecialPairArea(a, b, move1, move2);
                if (area < bestArea) {
                    var decision1 = new Decision(a, MoveType.NEGATIVE, area, true);
                    var decision2 = new Decision(b, MoveType.POSITIVE, area, true);
                    best = List.of(decision1, decision2);
                }
            }
        }

        return best;
    }

    private double calculateSpecialPairArea(Configuration c1, Configuration c2, NormalMove move1, NormalMove move2) {
        final var sharedLength = c1.next.length();
        final var sharedDirection = c1.next.getDirection();

        final var height1 = move1.getDistance();
        final var height2 = move2.getDistance();

        final var innerLength1 = c1.inner.length();
        final var innerLength2 = c2.inner.length();

        final var outerLength1 = move1.getContraction().length();
        final var outerLength2 = move2.getContraction().length();

        // Todo: better naming of both ratios
        final var ratio1 = (outerLength1 - innerLength1) / height1;
        final var ratio2 = (outerLength2 - innerLength2) / height2;

        final var angle1 = Vector.dotProduct(move1.getDirection(), sharedDirection);
        final var angle2 = Vector.dotProduct(move2.getDirection(), Vector.multiply(-1, sharedDirection));

        var a = angle1 * angle1 * ratio1 - angle2 * angle2 * ratio2;
        var b = 2 * angle1 * innerLength1 + 2 * angle2 * angle2 * sharedLength * ratio2 + 2 * angle2 * innerLength2;
        var c = - angle2 * angle2 * sharedLength * sharedLength * ratio2 - 2 * angle2 * innerLength2 * sharedLength;

        var distance1 = DoubleUtil.solveQuadraticEquationForSmallestPositive(a, b, c, DoubleUtil.EPS);
        var distance2 = sharedLength - distance1;

        assert distance1 > 0 && distance1 < sharedLength : distance1 + " not in <0.0 , " + sharedLength + ">";
        assert distance2 > 0 && distance2 < sharedLength : distance2 + " not in <0.0 ," + sharedLength + ">";
        assert !DoubleUtil.close(distance1, 0) && !DoubleUtil.close(distance2, 0);

        // Find areas of potential moves. Take into account that move cant be larger then full contraction.
        var area1 = distance1 * angle1 <= move1.getDistance() + DoubleUtil.EPS ? move1.getAreaForDistance(distance1 * angle1) : move1.getArea();
        var area2 = distance2 * angle2 <= move2.getDistance() + DoubleUtil.EPS ? move2.getAreaForDistance(distance2 * angle2) : move2.getArea();

        var expectedArea1 = angle1 * distance1 * 0.5 * (angle1 * distance1 * ratio1 + 2 * innerLength1);
        var expectedArea2 = angle2 * distance2 * 0.5 * (angle2 * distance2 * ratio2 + 2 * innerLength2);

        assert DoubleUtil.close(expectedArea1, expectedArea2) : expectedArea1 + "!=" + expectedArea2;

        // move areas should always be equal thus actual move is limited by the smallest one
        return Math.min(area1, area2);
    }

    private boolean doCollide(Configuration positive, Configuration negative) {
        return undirectedEquals(negative.inner, positive.previous)
                || undirectedEquals(negative.next, positive.previous)
                || undirectedEquals(negative.inner, positive.inner)
                || undirectedEquals(positive.inner, negative.previous)
                || undirectedEquals(positive.next, negative.previous);

        // Ignores valid cases where (next == previous or previous == next) && the shared edge has convex and reflex corners
        // These are handled by special neighbouring case
    }

    private boolean areNeighbouring(Configuration first, Configuration second) {
        return (undirectedEquals(second.next, first.previous) && ((second.isEndReflex() && first.isStartConvex()) || (second.isEndConvex() && first.isStartReflex())))
                || (undirectedEquals(first.next, second.previous) && ((first.isEndReflex() && second.isStartConvex()) || (first.isEndConvex() && second.isStartReflex())));
    }
}
