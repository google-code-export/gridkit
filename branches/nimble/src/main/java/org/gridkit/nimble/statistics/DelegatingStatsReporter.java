package org.gridkit.nimble.statistics;

import java.util.Map;

public class DelegatingStatsReporter implements FlushableStatsReporter {
    private final StatsReporter delegate;

    public DelegatingStatsReporter(StatsReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void report(Map<String, Object> stats) {
        delegate.report(stats);
    }
    
    protected StatsReporter getDelegate() {
        return delegate;
    }
    
    @Override
    public void flush() {
        if (delegate instanceof FlushableStatsReporter) {
            ((FlushableStatsReporter)delegate).flush();
        }
    }
}
