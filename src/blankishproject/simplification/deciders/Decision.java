package blankishproject.simplification.deciders;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.moves.Move;
import blankishproject.simplification.moves.MoveType;

public class Decision {

    public final Configuration configuration;
    public final Move move;

    public final boolean requiresCleanup;
    public final double removeArea;

    public Decision(Configuration configuration, Move move) {
        this.configuration = configuration;
        this.move = move;
        this.requiresCleanup = true;
        this.removeArea = move.getAffectedArea();
    }

    public Decision(Configuration configuration, Move move, double removeArea) {
        this(configuration, move, removeArea, true);
    }

    public Decision(Configuration configuration, Move move, double removeArea, boolean requiresCleanup) {
        this.configuration = configuration;
        this.move = move;
        this.requiresCleanup = requiresCleanup;
        this.removeArea = removeArea;
    }

}
