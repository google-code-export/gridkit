package org.gridkit.search.gemfire.benchmark.task;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gridkit.search.gemfire.benchmark.FtsData;

import java.util.HashMap;
import java.util.Map;

public abstract class BenchmarkTask {
    protected Map<String, DescriptiveStatistics> statistics =
        new HashMap<String, DescriptiveStatistics>();

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
}
