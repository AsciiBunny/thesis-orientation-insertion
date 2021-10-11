package blankishproject.simplification.moves;

import java.util.List;

public abstract class Move {

    public abstract MoveType getType();

    public abstract boolean isValid();

    public abstract double getAffectedArea();


    public abstract void applyForArea(double area);

    public abstract void apply();
}
