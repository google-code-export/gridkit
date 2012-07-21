package org.gridkit.nimble.statistics;

public interface StatsReporter {
    void report(String statistica , double value);
    
    void report(String statistica , long timestamp, double value);

    void report(String message, long timestamp, Throwable throwable);
}
