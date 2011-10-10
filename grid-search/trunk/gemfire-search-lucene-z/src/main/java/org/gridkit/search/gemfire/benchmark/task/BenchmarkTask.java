package org.gridkit.search.gemfire.benchmark.task;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gridkit.search.gemfire.benchmark.FtsData;

public abstract class BenchmarkTask {
    protected Map<String, DescriptiveStatistics> statistics =
        new TreeMap<String, DescriptiveStatistics>();

    protected FtsData ftsData;

    public void reset() {
        statistics.clear();
    }

    public abstract boolean execute() throws Exception;

    public void record() {}

    public void setFtsData(FtsData ftsData) {
        this.ftsData = ftsData;
    }

    public Map<String, DescriptiveStatistics> getStatistics() {
        return statistics;
    }
    
    public DescriptiveStatistics getStatistics(String key) {
        DescriptiveStatistics result = statistics.get(key);
        
        if (result == null) {
            result = new DescriptiveStatistics();
            statistics.put(key, result);
        }
        
        return result;
    }
    
    protected DescriptiveStatistics getStatistics(String prefix, Integer count) {
        return getStatistics(prefix + "." + count);
    }
}
