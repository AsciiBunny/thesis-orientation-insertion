package blankishproject.deciders;

import blankishproject.edgelist.Configuration;
import blankishproject.edgelist.ConfigurationList;
import blankishproject.moves.MoveType;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.Collections;
import java.util.List;

public class OnlyPositiveDecider implements IDecider {

    @Override
    public List<Decision> findContraction(Polygon polygon, ConfigurationList configurations) {
        Configuration min = null;
        double minArea = Double.MAX_VALUE;
        for (var configuration : configurations) {
            if (configuration.positiveNormalMove.hasValidContraction()) {
                var area = Math.abs(configuration.positiveNormalMove.getArea());
                if (area < minArea) {
                    min = configuration;
                    minArea = area;
                }
            }
        }
        return min != null ? Collections.singletonList(new Decision(min, MoveType.POSITIVE)) : Collections.emptyList();
    }
}
