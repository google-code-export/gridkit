package org.gridkit.nimble.statistics;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

public class DelegatingStatisticalSummary implements StatisticalSummary {
    private final StatisticalSummary delegate;

    public DelegatingStatisticalSummary(StatisticalSummary delegate) {
        this.delegate = delegate;
    }

    @Override
    public double getMean() {
        return delegate.getMean();
    }

    @Override
    public double getVariance() {
        return delegate.getVariance();
    }

    @Override
    public double getStandardDeviation() {
        return delegate.getStandardDeviation();
    }

    @Override
    public double getMax() {
        return delegate.getMax();
    }

    @Override
    public double getMin() {
        return delegate.getMin();
    }

    @Override
    public long getN() {
        return delegate.getN();
    }

    @Override
    public double getSum() {
        return delegate.getSum();
    }
    
    protected StatisticalSummary getDelegate() {
        return delegate;
    }
    
    @Override
    public String toString() {
        StringBuilder outBuffer = new StringBuilder();
        String endl = "\n";
        outBuffer.append("StatisticalSummary:").append(endl);
        outBuffer.append("n: ").append(getN()).append(endl);
        outBuffer.append("min: ").append(getMin()).append(endl);
        outBuffer.append("max: ").append(getMax()).append(endl);
        outBuffer.append("mean: ").append(getMean()).append(endl);
        outBuffer.append("variance: ").append(getVariance()).append(endl);
        outBuffer.append("standard deviation: ").append(getStandardDeviation());
        return outBuffer.toString();
    }
}
