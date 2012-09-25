package org.gridkit.nimble.statistics.simple;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.DelegatingStatisticalSummary;
import org.gridkit.nimble.statistics.StatsOps;
import org.gridkit.nimble.statistics.ThroughputSummary;

public class SimpleThroughputSummary extends DelegatingStatisticalSummary implements ThroughputSummary {    
    private double startMs;
    private double finishMs;
    
    public SimpleThroughputSummary(StatisticalSummary valStats, double scale, double startMs, double finishMs) {
        super(StatsOps.scale(valStats, scale));
        this.startMs = startMs;
        this.finishMs = finishMs;
    }

    @Override
    public double getThroughput(TimeUnit timeUnit) {
        return getSum() / getDuration(timeUnit);
    }

    @Override
    public double getDuration(TimeUnit timeUnit) {
        return Math.max((finishMs - startMs), 1) * StatsOps.getScale(TimeUnit.MILLISECONDS, timeUnit);
    }

    @Override
    public double getStartTime(TimeUnit timeUnit) {
        return StatsOps.convert(startMs, TimeUnit.MILLISECONDS, timeUnit);
    }

    @Override
    public double getFinishTime(TimeUnit timeUnit) {
        return StatsOps.convert(finishMs, TimeUnit.MILLISECONDS, timeUnit);
    }
}
