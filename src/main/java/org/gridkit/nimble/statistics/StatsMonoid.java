package org.gridkit.nimble.statistics;

public interface StatsMonoid<T> {
    StatsProducer<T> newStatsProducer();
    
    T emptyStats();
    
    T combine(T stats1, T stats2);
}
