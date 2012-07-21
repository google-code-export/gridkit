package org.gridkit.nimble.statistics;

public interface StatsFactory<T> {
    StatsProducer<T> newStatsProducer();
    
    T emptyStats();
    
    T combine(T stats1, T stats2);
}
