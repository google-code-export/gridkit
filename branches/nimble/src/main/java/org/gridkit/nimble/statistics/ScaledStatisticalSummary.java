package org.gridkit.nimble.statistics;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

public class ScaledStatisticalSummary extends DelegatingStatisticalSummary {
    private final double scale;
    private final double square;
    
    public ScaledStatisticalSummary(StatisticalSummary delegate, double scale) {
        super(delegate);
        this.scale = scale;
        this.square = scale * scale;
    }
    
    @Override
    public double getMean() {
        return getDelegate().getMean() * scale;
    }

    @Override
    public double getVariance() {
        return getDelegate().getVariance() * square;
    }

    @Override
    public double getStandardDeviation() {
        return getDelegate().getStandardDeviation() * scale;
    }

    @Override
    public double getMax() {
        return getDelegate().getMax() * scale;
    }

    @Override
    public double getMin() {
        return getDelegate().getMin() * scale;
    }

    @Override
    public double getSum() {
        return getDelegate().getSum() * scale;
    }
}
