package blankishproject.simplification;

import blankishproject.OrientationSet;
import blankishproject.Util;
import blankishproject.simplification.moves.moving.NegativeNormalMove;
import blankishproject.simplification.moves.moving.PairNormalMove;
import blankishproject.simplification.moves.moving.PositiveNormalMove;
import blankishproject.simplification.moves.rotation.EndRotationMove;
import blankishproject.simplification.moves.rotation.MiddleRotationMove;
import blankishproject.simplification.moves.rotation.RotationMove;
import blankishproject.simplification.moves.rotation.StartRotationMove;
import blankishproject.ui.ProgressDialog;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimplificationData {

    public Polygon polygon;
    public OrientationSet orientations = new OrientationSet();

    public ArrayList<Configuration> configurations;

    public ArrayList<PositiveNormalMove> positiveMoves;
    public ArrayList<NegativeNormalMove> negativeMoves;

    public ArrayList<PairNormalMove> positivePairMoves;
    public ArrayList<PairNormalMove> negativePairMoves;

    public ArrayList<RotationMove> startRotationMoves;
    public ArrayList<RotationMove> endRotationMoves;
    public ArrayList<RotationMove> middleRotationMoves;

    public List<Staircase> staircases;

    public String deciderType = "4. Minimal Complementary Pair";

    private ProgressDialog dialog;
    public boolean runThreaded = true;

    //region Calculate Settings
    public boolean calculateStartRotationMoves = true;
    public boolean calculateEndRotationMoves = true;
    public boolean calculateMiddleRotationMoves = true;

    public boolean compensateSingleMoves = true;

    public int minStaircaseSize = 5;
    //endregion Calculate Settings

    // region Debug Drawing Settings
    public int selectedEdge = -1;

    public boolean drawForAll = false;
    public boolean drawConvexityArcs = false;
    public boolean drawConvexityEdges = false;
    public boolean drawPositiveContractions = false;
    public boolean drawNegativeContractions = false;
    public boolean drawPositivePairs = false;
    public boolean drawNegativePairs = false;
    public boolean drawBlockingPoints = false;

    public boolean drawStartRotations = false;
    public boolean drawEndRotations = false;
    public boolean drawMiddleRotations = false;

    public boolean drawInnerDifference = false;
    public boolean drawOuterDifference = false;
    // endregions Debug Drawing Settings

    public SimplificationData(Polygon polygon) {
        orientations.addOrientationDegrees(0);
        orientations.addOrientationDegrees(45);
        orientations.addOrientationDegrees(90);
        orientations.addOrientationDegrees(135);

        init(polygon, null);
    }

    public void reset() {
        configurations = new ArrayList<>();
        positiveMoves = new ArrayList<>();
        negativeMoves = new ArrayList<>();
        positivePairMoves = new ArrayList<>();
        negativePairMoves = new ArrayList<>();
        startRotationMoves = new ArrayList<>();
        endRotationMoves = new ArrayList<>();
        middleRotationMoves = new ArrayList<>();
    }

    public void init(Polygon polygon, ProgressDialog dialog) {
        this.polygon = polygon;

        this.dialog = dialog;
        if (dialog != null) {
            var count = 5 + Util.countTrue(calculateStartRotationMoves, calculateEndRotationMoves, calculateMiddleRotationMoves);
            dialog.setMaxProgress(polygon.vertexCount() * count);
        }

        configurations = initConfigurations();

        positiveMoves = initPositiveMoves();
        negativeMoves = initNegativeMoves();
        initAllSpecialPairs();

        recalculateStaircases();

        if (calculateStartRotationMoves)
            startRotationMoves = initStartRotationMoves();
        if (calculateEndRotationMoves)
            endRotationMoves = initEndRotationMoves();
        if (calculateMiddleRotationMoves)
            middleRotationMoves = initMiddleRotationMoves();

        this.dialog = null;
    }

    public LineSegment removeAtIndex(int index) {
        configurations.remove(index);
        positiveMoves.remove(index);
        negativeMoves.remove(index);
        positivePairMoves.remove(index);
        negativePairMoves.remove(index);

        if (calculateStartRotationMoves)
            startRotationMoves.remove(index);
        if (calculateEndRotationMoves)
            endRotationMoves.remove(index);
        if (calculateMiddleRotationMoves)
            middleRotationMoves.remove(index);

        var edge = polygon.edge(index).clone();
        polygon.removeVertex(index);
        return edge;
    }

    public void resetAtIndex(int index) {
        assert configurations.get(index).index == index;

        var configuration = new Configuration(polygon, index);
        configurations.set(index, configuration);

        positiveMoves.set(index, new PositiveNormalMove(configuration, polygon));
        negativeMoves.set(index, new NegativeNormalMove(configuration, polygon));
        positivePairMoves.set(index, new PairNormalMove(this, configuration, true));
        negativePairMoves.set(index, new PairNormalMove(this, configuration, true));

        if (calculateStartRotationMoves)
            startRotationMoves.set(index, new StartRotationMove(configuration, orientations));
        if (calculateEndRotationMoves)
            endRotationMoves.set(index, new EndRotationMove(configuration, orientations));
        if (calculateMiddleRotationMoves)
            middleRotationMoves.set(index, new MiddleRotationMove(configuration, orientations));

    }

    private ArrayList<Configuration> initConfigurations() {
        var list = new ArrayList<Configuration>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new Configuration(polygon, index));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }

    private ArrayList<PositiveNormalMove> initPositiveMoves() {
        var list = new ArrayList<PositiveNormalMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new PositiveNormalMove(configurations.get(index), polygon));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }

    private ArrayList<NegativeNormalMove> initNegativeMoves() {
        var list = new ArrayList<NegativeNormalMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new NegativeNormalMove(configurations.get(index), polygon));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }


    public void initAllSpecialPairs() {
        positivePairMoves = new ArrayList<>(polygon.vertexCount());
        negativePairMoves = new ArrayList<>(polygon.vertexCount());

        for (int index = 0; index < polygon.vertexCount(); index++) {
            initSpecialPairs(index);
            if (dialog != null)
                dialog.increaseProgress(1);
        }
    }

    public void initSpecialPairs(int index) {
        var moves = getSpecialPairs(index);

        positivePairMoves.add(moves[0]);
        negativePairMoves.add(moves[1]);
    }

    public void resetSpecialPairs(int index) {
        var moves = getSpecialPairs(index);

        positivePairMoves.set(index, moves[0]);
        negativePairMoves.set(index, moves[1]);
    }

    public PairNormalMove[] getSpecialPairs(int indexA) {
        var indexB = (indexA + 2) % configurations.size();

        var configurationA = configurations.get(indexA);
        var configurationB = configurations.get(indexB);
        var positiveMoveA = positiveMoves.get(indexA);
        var positiveMoveB = positiveMoves.get(indexB);
        var negativeMoveA = negativeMoves.get(indexA);
        var negativeMoveB = negativeMoves.get(indexB);

        // Set up moves as invalid so that they are easier to check against in the list
        PairNormalMove positivePairMove = new PairNormalMove(this, configurationA, true);
        PairNormalMove negativePairMove = new PairNormalMove(this, configurationA, true);

        if (configurationA.isSpecialPairNeighbouring(configurationB)) {
            if (positiveMoveA.hasValidContraction() && negativeMoveB.hasValidContraction()) {
                positivePairMove = new PairNormalMove(configurationA, configurationB, positiveMoveA, negativeMoveB, this);
            }

            if (negativeMoveA.hasValidContraction() && positiveMoveB.hasValidContraction()) {
                negativePairMove = new PairNormalMove(configurationA, configurationB, negativeMoveA, positiveMoveB, this);
            }
        }

        return new PairNormalMove[]{positivePairMove, negativePairMove};
    }

    private ArrayList<RotationMove> initStartRotationMoves() {
        var list = new ArrayList<RotationMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new StartRotationMove(configurations.get(index), orientations));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }

    private ArrayList<RotationMove> initEndRotationMoves() {
        var list = new ArrayList<RotationMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new EndRotationMove(configurations.get(index), orientations));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }

    private ArrayList<RotationMove> initMiddleRotationMoves() {
        var list = new ArrayList<RotationMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new MiddleRotationMove(configurations.get(index), orientations));
            if (dialog != null)
                dialog.increaseProgress(1);
        }
        return list;
    }

    public void recalculateStaircases() {
        staircases = new ArrayList<>();
        var current = new Staircase();
        for (int i = 0; i < configurations.size(); i++) {
            var edge = configurations.get(i);
            if (edge.isInnerConvex() || edge.isInnerReflex()) {
                if (current.start >= 0) {
                    current.end = i - 1;
                    staircases.add(current);
                    current.listLength = configurations.size();
                    current = new Staircase();
                }
            } else {
                if (current.start < 0) {
                    current.start = i;
                }
            }
        }

        if (current.start >= 0 && current.end < 0) {
            staircases.get(0).loops = true;
            staircases.get(0).start = current.start;
        }

        staircases = staircases.stream().filter(staircase -> staircase.length() >= minStaircaseSize).collect(Collectors.toList());
    }
}
