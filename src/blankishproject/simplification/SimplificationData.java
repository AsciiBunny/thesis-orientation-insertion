package blankishproject.simplification;

import blankishproject.simplification.moves.NegativeNormalMove;
import blankishproject.simplification.moves.PairNormalMove;
import blankishproject.simplification.moves.PositiveNormalMove;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.ArrayList;

public class SimplificationData {

    public Polygon polygon;
    public ArrayList<Configuration> configurations;

    public ArrayList<PositiveNormalMove> positiveMoves;
    public ArrayList<NegativeNormalMove> negativeMoves;

    public ArrayList<PairNormalMove> positivePairMoves;
    public ArrayList<PairNormalMove> negativePairMoves;

    public String deciderType = "4. Minimal Complementary Pair";

    // region Debug Drawing Settings
    public boolean drawConvexityArcs = false;
    public boolean drawConvexityEdges = false;
    public boolean drawPositiveContractions = false;
    public boolean drawNegativeContractions = false;
    public boolean drawBlockingPoints = false;

    public boolean drawInnerDifference = false;
    public boolean drawOuterDifference = false;
    // endregions Debug Drawing Settings

    public SimplificationData(Polygon polygon) {
        init(polygon);
    }

    public void init(Polygon polygon) {
        this.polygon = polygon;

        configurations = initConfigurations();

        positiveMoves = initPositiveMoves();
        negativeMoves = initNegativeMoves();

        initAllSpecialPairs();
    }

    public void removeAtIndex(int index) {
        polygon.removeVertex(index);
        configurations.remove(index);
        positiveMoves.remove(index);
        negativeMoves.remove(index);
        positivePairMoves.remove(index);
        negativePairMoves.remove(index);
    }

    public void resetAtIndex(int index) {
        assert configurations.get(index).index == index;

        var configuration = new Configuration(polygon, index);
        configurations.set(index, configuration);
        positiveMoves.set(index, new PositiveNormalMove(configuration, polygon));
        negativeMoves.set(index, new NegativeNormalMove(configuration, polygon));
        positivePairMoves.set(index, new PairNormalMove(this, configuration, true));
        negativePairMoves.set(index, new PairNormalMove(this, configuration, true));
    }

    private ArrayList<Configuration> initConfigurations() {
        var list = new ArrayList<Configuration>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new Configuration(polygon, index));
        }
        return list;
    }

    private ArrayList<PositiveNormalMove> initPositiveMoves() {
        var list = new ArrayList<PositiveNormalMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new PositiveNormalMove(configurations.get(index), polygon));
        }
        return list;
    }

    private ArrayList<NegativeNormalMove> initNegativeMoves() {
        var list = new ArrayList<NegativeNormalMove>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new NegativeNormalMove(configurations.get(index), polygon));
        }
        return list;
    }


    public void initAllSpecialPairs() {
        positivePairMoves = new ArrayList<>(polygon.vertexCount());
        negativePairMoves = new ArrayList<>(polygon.vertexCount());

        for (int index = 0; index < polygon.vertexCount(); index++) {
            initSpecialPairs(index);
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
}