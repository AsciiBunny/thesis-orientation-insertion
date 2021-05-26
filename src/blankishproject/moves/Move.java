package blankishproject.moves;

public abstract class Move {

    public abstract MoveType getType();

    public abstract boolean isValid();

    public abstract double getAffectedArea();


    public abstract void applyForArea(double area);

    public abstract void apply();
}
