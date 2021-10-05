package blankishproject.simplification;

import blankishproject.simplification.moves.PairNormalMove;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.ArrayList;

public class SimplificationData {

    public Polygon polygon;
    public ArrayList<Configuration> configurations;

    public String deciderType = "4. Minimal Complementary Pair";

    public boolean drawConvexityArcs = false;
    public boolean drawConvexityEdges = false;
    public boolean drawPositiveContractions = false;
    public boolean drawNegativeContractions = false;
    public boolean drawBlockingPoints = false;

    public boolean drawInnerDifference = false;
    public boolean drawOuterDifference = false;

    public SimplificationData(Polygon polygon) {
        init(polygon);
    }

    public void init(Polygon polygon) {
        this.polygon = polygon;
        configurations = initConfigurations();
        initAllSpecialPairs();
    }

    private ArrayList<Configuration> initConfigurations() {
        var list = new ArrayList<Configuration>(polygon.vertexCount());
        for (int index = 0; index < polygon.vertexCount(); index++) {
            list.add(new Configuration(polygon, index));
        }


        return list;
    }

    public void initAllSpecialPairs() {
        for (int index = 0; index < polygon.vertexCount(); index++) {
            initSpecialPairs(index);
        }
    }

    public void initSpecialPairs(int index) {
        var a = configurations.get((index - 2 + configurations.size()) % configurations.size());
        var b = configurations.get(index);

        a.positivePairMove = null;
        a.negativePairMove = null;

        if (!a.isSpecialPairNeighbouring(b))
            return;

        if (a.positiveNormalMove.hasValidContraction() && b.negativeNormalMove.hasValidContraction()) {
            a.positivePairMove = new PairNormalMove(a, b, a.positiveNormalMove, b.negativeNormalMove, this);
        }

        if (a.negativeNormalMove.hasValidContraction() && b.positiveNormalMove.hasValidContraction()) {
            a.negativePairMove = new PairNormalMove(a, b, a.negativeNormalMove, b.positiveNormalMove, this);
        }
    }
}
