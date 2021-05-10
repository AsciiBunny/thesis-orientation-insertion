package blankishproject.edgelist;

import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationList implements Iterable<Configuration> {

    public final Polygon polygon;
    //public final List<LineSegment> edges;
    private final List<Configuration> list;

    public ConfigurationList(Polygon polygon) {
        this.polygon = polygon;
        var edges = new ArrayList<LineSegment>();
        polygon.edges().forEach(edges::add);

        this.list = new ArrayList<>();
        for (int index = 0; index < edges.size(); index++) {
            list.add(new Configuration(polygon, index, edges.get((index + edges.size() - 1) % edges.size()), edges.get(index % edges.size()), edges.get((index + 1) % edges.size())));
        }

    }

    @Override
    public Iterator<Configuration> iterator() {
        return new ConfigurationIterator(this);
    }

    public int size() {
        return list.size();
    }

    public Configuration get(int index) {
        return list.get(index);
    }
}

