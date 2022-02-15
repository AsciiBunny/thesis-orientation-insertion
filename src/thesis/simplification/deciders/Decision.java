package thesis.simplification.deciders;

import thesis.simplification.Configuration;
import thesis.simplification.moves.Move;

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
