package blankishproject.edgelist;

import blankishproject.Data;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationList implements Iterable<Configuration> {

    public final Data data;
    private final List<Configuration> list;

    public ConfigurationList(Data data) {
        this.data = data;
        this.list = new ArrayList<>();
        for (int index = 0; index < data.simplification.vertexCount(); index++) {
            list.add(new Configuration(data, index));
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

    public boolean remove(Configuration configuration) {
        return list.remove(configuration);
    }
}

