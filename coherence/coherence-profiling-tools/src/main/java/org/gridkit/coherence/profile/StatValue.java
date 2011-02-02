/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface StatValue {

    public double getCount();

    public double getAvg();

    public double getTotal();

    public double getStdDev();
    
    public double getMax();
    
    public double getApproximatePercentile95();
    
    public double getApproximatePercentile99();
    
    public double getApproximatePercentile999();
}
