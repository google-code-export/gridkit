package org.gridkit.coherence.profile.jmx;

import org.gridkit.coherence.profile.StatValue;

import javax.management.MXBean;
import java.util.Map;

/**
 * @author Dmitri Babaev
 */
@MXBean
public interface MetricsMxBean {
	
    public Map<String, StatValue> getMetricStats();
    
    public void resetByRegExPattern(String regexPattern);
    
    public void resetMetric(String name);
    
    public void enableProfiling();
    
    public void disableProfiling();
}
