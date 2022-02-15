package thesis.simplification;

public class Staircase {

    public int start = -1;
    public int end = -1;
    public int listLength = -1;
    public boolean loops = false;

    public int length() {
        if (loops)
            return end + 1 + (listLength - start);

        return end - start + 1;
    }
}
