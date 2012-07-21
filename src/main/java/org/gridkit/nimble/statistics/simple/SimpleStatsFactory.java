package org.gridkit.nimble.statistics.simple;

import org.gridkit.nimble.statistics.StatsFactory;
import org.gridkit.nimble.statistics.StatsProducer;

public class SimpleStatsFactory implements StatsFactory<SimpleStats> {
    @Override
    public StatsProducer<SimpleStats> newStatsProducer() {
        return null;
    }

    @Override
    public SimpleStats emptyStats() {
        return null;
    }

    @Override
    public SimpleStats combine(SimpleStats stats1, SimpleStats stats2) {
        return null;
    }
}
