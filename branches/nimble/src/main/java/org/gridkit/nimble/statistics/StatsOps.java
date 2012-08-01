package org.gridkit.nimble.statistics;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class StatsOps {
    private static final StatisticalSummary emptySummary = (new SummaryStatistics()).getSummary();
    
    public static StatisticalSummary combine(StatisticalSummary s1, StatisticalSummary s2) {
        if (s1.getN() == 0){
            return s2;
        } else if (s2.getN() == 0) {
            return s1;
        } else if (s1.getN() == 0 && s2.getN() == 0) {
            return emptySummary;
        }
        
        long n = s1.getN() + s2.getN();
        
        double mean = (s1.getN() * s1.getMean() + s2.getN() * s2.getMean()) / n;
        
        double s1Diff = (mean - s1.getMean()) * (mean - s1.getMean());
        double s2Diff = (mean - s2.getMean()) * (mean - s2.getMean());
        
        double var = (s1.getN() * (s1.getVariance() + s1Diff) + s2.getN() * (s2.getVariance() + s2Diff)) / n;
        
        double sum = s1.getSum() + s2.getSum();
        
        double max = Math.max(s1.getMax(), s2.getMax());
        double min = Math.min(s1.getMin(), s2.getMin());
        
        return new StatisticalSummaryValues(mean, var, n, max, min, sum);
    }
}
