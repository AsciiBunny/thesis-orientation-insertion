package blankishproject.simplification.deciders;

import blankishproject.simplification.Configuration;
import blankishproject.simplification.moves.MoveType;

public class Decision {

    public final Configuration configuration;
    public final MoveType type;

    public final boolean requiresCleanup;
    public final double removeArea;

    public Decision(Configuration configuration, MoveType type) {
        this.configuration = configuration;
        this.type = type;
        this.requiresCleanup = true;
        this.removeArea = configuration.getMove(type).getAffectedArea();
    }

    public Decision(Configuration configuration, MoveType type, double removeArea) {
        this(configuration, type, removeArea, false);
    }

    public Decision(Configuration configuration, MoveType type, double removeArea, boolean requiresCleanup) {
        this.configuration = configuration;
        this.type = type;
        this.requiresCleanup = requiresCleanup;
        this.removeArea = removeArea;
    }

}
