package org.gridkit.nimble.statistics;

public interface StatsProducer<T> extends StatsReporter {
    T produce();
}
