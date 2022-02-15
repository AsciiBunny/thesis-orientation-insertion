package thesis.simplification.moves;

import thesis.simplification.Configuration;

public abstract class Move {

    public final Configuration configuration;

    public Move(Configuration configuration) {
        this.configuration = configuration;
    }

    public abstract MoveType getType();

    public abstract boolean isValid();

    public abstract double getAffectedArea();


    public abstract void applyForArea(double area);

    public abstract void apply();

    public abstract double getCompensationArea();
}
