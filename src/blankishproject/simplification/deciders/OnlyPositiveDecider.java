package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.PositiveNormalMove;

import java.util.Collections;
import java.util.List;

public class OnlyPositiveDecider implements IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var moves = data.positiveMoves;
        PositiveNormalMove min = null;
        double minArea = Double.MAX_VALUE;
        for (var move : moves) {
            if (move.hasValidContraction()) {
                var area = Math.abs(move.getArea());
                if (area < minArea) {
                    min = move;
                    minArea = area;
                }
            }
        }
        return min != null ? Collections.singletonList(new Decision(min.configuration, min)) : Collections.emptyList();
    }
}
