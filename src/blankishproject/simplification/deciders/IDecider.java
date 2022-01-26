package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.Move;
import blankishproject.simplification.moves.moving.NormalMove;

import java.util.List;
import java.util.Map;

public abstract class IDecider {

    public static Map<String, IDecider> deciders = Map.ofEntries(
            Map.entry("0. Only Positive", new OnlyPositiveDecider()),
            Map.entry("1. Only Negative", new OnlyNegativeDecider()),
            Map.entry("2. Smallest Edge-Move", new SmallestSingleDecider()),
            Map.entry("3. Minimal Pair", new MinimalPairDecider()),
            Map.entry("4. Minimal Complementary Pair", new MinimalComplementaryPairDecider()),
            Map.entry("5. Only Start/End Rotation", new OnlyStartEndRotationDecider()),
            Map.entry("6. Only Middle Rotation", new OnlyMiddleRotationDecider()),
            Map.entry("7. Minimal", new MinimalDecider())
    );

    public abstract List<Decision> findMoves(SimplificationData data);

    protected <K extends Move> K findSmallest(List<K> moves) {
        double minArea = Double.MAX_VALUE;
        K min = null;
        for (var move : moves) {
            if (move.isValid()) {
                var area = move.getAffectedArea();
                if (area < minArea) {
                    min = move;
                    minArea = area;
                }
            }
        }
        return min;
    }

    protected Decision findCompensator(SimplificationData data, Move move, int index) {
        double area = move.getCompensationArea();
        var moves = area > 0 ? data.negativeMoves : data.positiveMoves;

        double minArea = Double.MAX_VALUE;
        NormalMove smallest = null;
        for (var m : moves) {
            if (m.isValid()) {
                var a = m.getAffectedArea();
                if (a < minArea && a >= Math.abs(area)
                        && !m.configuration.isColliding(move.configuration)) {
                    smallest = m;
                    minArea = a;
                }
            }
        }

        return new Decision(smallest.configuration, smallest, Math.abs(area));
    }

    protected <K extends Move> K getSmallest(K move1, K move2) {
        if (move1 == null)
            return move2;
        if (move2 == null)
            return move1;

        if (move1.getAffectedArea() <= move2.getAffectedArea())
            return move1;

        return move2;
    }

}

