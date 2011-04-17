/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.distributed;

import java.util.Map;

import org.gridkit.coherence.profile.Sampler;
import org.gridkit.coherence.profile.StatValue;


/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface HistogramService {

    public Sampler defineSampler(String name, long scale, long min, long max, int size);
    
    public StatValue getValue(String name);
    
    public void resetValue(String name);

    public Map<String, StatValue> getAll(String pattern);
    
    public void resetAll(String pattern);
}
