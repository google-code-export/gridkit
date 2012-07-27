package org.gridkit.nimble.statistics;

import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

public interface ThroughputSummary extends StatisticalSummary {
    double getThroughput(TimeUnit timeUnit);
    
    double getDuration(TimeUnit timeUnit);
}
