package org.gridkit.nimble.statistics.simple;

import java.io.Serializable;

import org.gridkit.nimble.statistics.StatsMonoid;
import org.gridkit.nimble.statistics.StatsProducer;

@SuppressWarnings("serial")
public class SimpleStatsFactory implements StatsMonoid<SimpleStats>, Serializable {    
    @Override
    public StatsProducer<SimpleStats> newStatsProducer() {
        return new SimpleStatsProducer();
    }

    @Override
    public SimpleStats emptyStats() {
        return new SimpleStats();
    }

    @Override
    public SimpleStats combine(SimpleStats s1, SimpleStats s2) {
        return SimpleStats.combine(s1, s2);
    }
}
