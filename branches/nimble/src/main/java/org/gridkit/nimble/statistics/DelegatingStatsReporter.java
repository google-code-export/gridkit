package org.gridkit.nimble.statistics;

public class DelegatingStatsReporter implements StatsReporter {
    private final StatsReporter delegate;

    public DelegatingStatsReporter(StatsReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void report(String statistica, double value) {
        delegate.report(statistica, value);
        
    }

    @Override
    public void report(String statistica, long timestamp, double value) {
        delegate.report(statistica, timestamp, value);
        
    }

    @Override
    public void report(String message, long timestamp, Throwable throwable) {
        delegate.report(message, timestamp, throwable);
    }
    
    protected StatsReporter getDelegate() {
        return delegate;
    }
}
