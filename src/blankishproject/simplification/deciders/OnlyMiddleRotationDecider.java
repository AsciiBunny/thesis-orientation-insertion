package blankishproject.simplification.deciders;

import blankishproject.simplification.SimplificationData;
import blankishproject.simplification.moves.rotation.RotationMove;

import java.util.Collections;
import java.util.List;

public class OnlyMiddleRotationDecider implements IDecider {

    @Override
    public List<Decision> findMoves(SimplificationData data) {
        var moves = data.middleRotationMoves;
        RotationMove min = null;
        double minArea = Double.MAX_VALUE;
        for (var move : moves) {
            if (move.isValid()) {
                var area = move.getAffectedArea();
                if (area < minArea) {
                    min = move;
                    minArea = area;
                }
            }
        }
        return min != null ? Collections.singletonList(new Decision(min.configuration, min)) : Collections.emptyList();
    }
}
