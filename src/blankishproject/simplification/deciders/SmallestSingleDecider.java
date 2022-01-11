package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.moving.NormalMove;

import java.util.Collections;
import java.util.List;

public class SmallestSingleDecider implements IDecider{

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var positiveMoves = data.positiveMoves;
        NormalMove min = null;
        double minArea = Double.MAX_VALUE;
        for (var move : positiveMoves) {
            if (move.hasValidContraction()) {
                var area = Math.abs(move.getArea());
                if (area < minArea) {
                    min = move;
                    minArea = area;
                }
            }
        }

        var negativeMoves = data.negativeMoves;
        for (var move : negativeMoves) {
            if (move.hasValidContraction()) {
                var area = Math.abs(move.getArea());
                if (area < minArea) {
                    min = move;
                    minArea = area;
                }
            }
        }

        return minArea < Double.MAX_VALUE ? Collections.singletonList(new Decision(min.configuration, min)) : Collections.emptyList();
    }
}
