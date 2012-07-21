package org.gridkit.nimble.statistics.simple;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.DelegatingStatisticalSummary;
import org.gridkit.nimble.statistics.ThroughputSummary;

public class SimpleThroughputSummary extends    DelegatingStatisticalSummary
                                     implements ThroughputSummary {
    public SimpleThroughputSummary(StatisticalSummary delegate) {
        super(delegate);
    }

    @Override
    public double getThroughput(TimeUnit timeUnit) {
        return getN()/getDuration(timeUnit);
    }

    @Override
    public long getDuration(TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.NANOSECONDS) {
            throw new IllegalArgumentException("timeUnit");
        }
        
        long scale = TimeUnit.MILLISECONDS.convert(1, timeUnit);
        
        double startTime = getMin();
        double finishTime = getMax();
        
        return (long)((finishTime - startTime) / scale);
    }
}
