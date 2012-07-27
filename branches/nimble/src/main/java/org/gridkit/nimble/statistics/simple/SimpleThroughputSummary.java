package org.gridkit.nimble.statistics.simple;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.DelegatingStatisticalSummary;
import org.gridkit.nimble.statistics.ThroughputSummary;

public class SimpleThroughputSummary extends    DelegatingStatisticalSummary
                                     implements ThroughputSummary {
    private StatisticalSummary finishStats;
    
    public SimpleThroughputSummary(StatisticalSummary delegate, StatisticalSummary finishStats) {
        super(delegate);
        this.finishStats = finishStats;
    }

    @Override
    public double getThroughput(TimeUnit timeUnit) {
        double n = getN();
        return n / getDuration(timeUnit);
    }

    @Override
    public double getDuration(TimeUnit timeUnit) {
        if (timeUnit == TimeUnit.NANOSECONDS || timeUnit == TimeUnit.MICROSECONDS) {
            throw new IllegalArgumentException("timeUnit");
        }
        
        long scale = TimeUnit.MILLISECONDS.convert(1, timeUnit);
        
        double startTime = getMin();
        double finishTime = finishStats.getMax();
        
        return (finishTime - startTime) / scale;
    }

    public static String getFinishStats(String stats) {
        return stats + "." + "finish";
    }
}
