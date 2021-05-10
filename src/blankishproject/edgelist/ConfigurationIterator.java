package blankishproject.edgelist;

import java.util.Iterator;
import java.util.List;

public class ConfigurationIterator implements Iterator<Configuration> {

    private final ConfigurationList configurationList;
    private int index = 0;

    public ConfigurationIterator(ConfigurationList configurationList) {
        this.configurationList = configurationList;
    }

    @Override
    public boolean hasNext() {
        return index < configurationList.size();
    }

    @Override
    public Configuration next() {
        return configurationList.get(index++);
    }
}
