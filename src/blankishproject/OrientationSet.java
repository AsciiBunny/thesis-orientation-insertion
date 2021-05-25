package blankishproject;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.*;

public class OrientationSet implements Iterable<OrientationSet.Orientation> {

    private final List<Orientation> orientations;

    public OrientationSet() {
        this.orientations = new ArrayList<>();
    }

    public OrientationSet(Collection<Double> orientations) {
        this.orientations = new ArrayList<>();
        orientations.forEach(this::addOrientation);
    }

    public void addOrientation(double orientation) {
        if (!isValid(orientation))
            throw new IllegalArgumentException();

        if (orientations.stream().anyMatch(a -> DoubleUtil.close(a.getOrientation(), orientation)))
            return;

        orientations.add(new Orientation(orientation));
        orientations.add(new Orientation((orientation + Math.PI) % (Math.PI * 2)));
        orientations.sort(Comparator.comparingDouble(Orientation::getOrientation));
        updateIndexes();
    }

    public void addOrientationDegrees(double orientation) {
        addOrientation((orientation % 180) / 180 * Math.PI);
    }

    private boolean isValid(double orientation) {
        return orientation >= 0 && orientation < 1 * Math.PI;
    }

    private void updateIndexes() {
        for (int i = 0; i < orientations.size(); i++) {
            var orientation = orientations.get(i);
            orientation.setIndex(i);
        }
    }

    public Orientation get(int index) {
        return orientations.get(index);
    }

    public Orientation getBefore(Double orientation) {
        var before = orientations.get(0);
        for (Orientation o : orientations) {
            if (DoubleUtil.close(orientation, o.orientation) || DoubleUtil.close(Math.abs(orientation - o.orientation), Math.PI * 2))
                return o;

            if (o.orientation < orientation)
                before = o;
            else
                break;
        }
        return before;
    }

    public Orientation getAfter(Double orientation) {
        for (Orientation o : orientations) {
            if (DoubleUtil.close(orientation, o.orientation) || DoubleUtil.close(Math.abs(orientation - o.orientation), Math.PI * 2))
                return o;

            if (o.orientation > orientation)
                return o;
        }
        return orientations.get(0);
    }

    public Orientation getClosest(Double orientation) {
        var before = getBefore(orientation);
        var after = getAfter(orientation);
        return Math.abs(before.getOrientation() - orientation) < Math.abs(after.getOrientation() - orientation + (after.index == 0 ? Math.PI * 2 : 0)) ? before : after;
    }

    @Override
    public Iterator<Orientation> iterator() {
        return orientations.iterator();
    }

    public int size() {
        return orientations.size();
    }

    class Orientation {
        private final double orientation;
        private final Vector direction;
        private int index;

        public Orientation(double orientation) {
            this.orientation = orientation;

            var dir = Vector.up();
            dir.rotate(-orientation);
            direction = dir;

            this.index = -1;
        }

        private void setIndex(int index) {
            this.index = index;
        }

        public double getOrientation() {
            return orientation;
        }

        public Vector getDirection() {
            return direction.clone();
        }

        public int getIndex() {
            return index;
        }

        public double getDistance(Vector direction) {
            return Math.min(this.direction.computeClockwiseAngleTo(direction), this.direction.computeCounterClockwiseAngleTo(direction));
        }

        @Override
        public String toString() {
            return "Orientation{" +
                    "orientation=" + orientation +
                    ", direction=" + direction +
                    ", index=" + index +
                    '}';
        }
    }
}
