package org.gridkit.nimble.statistics.simple;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.gridkit.nimble.util.ValidOps;

public class SimpleStatisticalSummary extends StatisticalSummaryValues {
    private static final long serialVersionUID = 3780143858221988272L;
    
    private final double m2;
    
    public SimpleStatisticalSummary() {
        this(Double.NaN, Double.NaN, Double.NaN, 0l, Double.NaN, Double.NaN, 0.0);
    }
    
    public SimpleStatisticalSummary(double mean, double variance, double m2, long n, double max, double min, double sum) {
        super(mean, variance, n, max, min, sum);
        this.m2 = m2;
    }

    public SimpleStatisticalSummary(SummaryStatistics stats) {
        this(
            stats.getMean(), stats.getVariance(), stats.getSecondMoment(),
            stats.getN(), stats.getMax(), stats.getMin(), stats.getSum()
        );
    }
    
    public double getSecondMoment() {
        return m2;
    }
    
    /**
     * based on {@link AggregateSummaryStatistics#aggregate(Collection)}
     */
    public static SimpleStatisticalSummary combine(Collection<SimpleStatisticalSummary> statistics) {
        ValidOps.notNull(statistics, "statistics");
        
        Iterator<SimpleStatisticalSummary> iterator = statistics.iterator();
        
        SimpleStatisticalSummary current = null;
        
        while (iterator.hasNext()) {
            current = iterator.next();
            
            if (current.getN() > 0) {
                break;
            }
        }
        
        if (current == null || current.getN() == 0) {
            return new SimpleStatisticalSummary();
        }
        
        long n = current.getN();
        double min = current.getMin();
        double sum = current.getSum();
        double max = current.getMax();
        double m2 = current.getSecondMoment();
        double mean = current.getMean();
        
        while (iterator.hasNext()) {
            current = iterator.next();
            
            if (current.getN() == 0) {
                continue;
            }
            
            if (current.getMin() < min || Double.isNaN(min)) {
                min = current.getMin();
            }
            
            if (current.getMax() > max || Double.isNaN(max)) {
                max = current.getMax();
            }
            
            sum += current.getSum();
            final double oldN = n;
            final double curN = current.getN();
            n += curN;
            final double meanDiff = current.getMean() - mean;
            mean = sum / n;
            m2 = m2 + current.getSecondMoment() + meanDiff * meanDiff * oldN * curN / n;
        }
        
        final double variance;
        
        if (n == 0) {
            variance = Double.NaN;
        } else if (n == 1) {
            variance = 0d;
        } else {
            variance = m2 / (n - 1);
        }
        
        return new SimpleStatisticalSummary(mean, variance, m2, n, max, min, sum);
    }
}
