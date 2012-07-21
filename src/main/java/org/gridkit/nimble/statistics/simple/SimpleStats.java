package org.gridkit.nimble.statistics.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.ScaledStatisticalSummary;
import org.gridkit.nimble.statistics.ThroughputSummary;

public class SimpleStats {
    private final Map<String, SimpleStatisticalSummary> valueStatsMap;
    private final Map<String, SimpleStatisticalSummary> timeStatsMap;

    public SimpleStats() {
        this(new HashMap<String, SimpleStatisticalSummary>(), new HashMap<String, SimpleStatisticalSummary>());
    }
    
    public SimpleStats(Map<String, SimpleStatisticalSummary> valueStatsMap, Map<String, SimpleStatisticalSummary> timeStatsMap) {
        this.valueStatsMap = valueStatsMap;
        this.timeStatsMap = timeStatsMap;
    }
    
    public StatisticalSummary getValueStats(String statistica) {
        return valueStatsMap.get(statistica);
    }
    
    public Set<String> getValueStatsNames() {
        return Collections.unmodifiableSet(valueStatsMap.keySet());
    }
    
    public StatisticalSummary getTimeStats(String statistica) {
        return timeStatsMap.get(statistica);
    }
    
    public Set<String> getTimeStatsNames() {
        return Collections.unmodifiableSet(timeStatsMap.keySet());
    }

    public StatisticalSummary getLatency(String statistica, TimeUnit timeUnit) {
        isTimeStats(statistica);
        
        double scale = 1.0 / TimeUnit.NANOSECONDS.convert(1, timeUnit);
        
        StatisticalSummary vs = getValueStats(statistica);
        
        return new ScaledStatisticalSummary(vs, scale);
    }
    
    public ThroughputSummary getThroughput(String statistica) {
        isTimeStats(statistica);
        
        return new SimpleThroughputSummary(getTimeStats(statistica));
    }
    
    private void isTimeStats(String statistica) {
        StatisticalSummary vs = getValueStats(statistica);
        StatisticalSummary ts  = getTimeStats(statistica);

        if (vs.getN() != ts.getN()) {
            throw new IllegalStateException(statistica + " is not time based statistics");
        }
    }
    
    public static SimpleStats combine(SimpleStats s1, SimpleStats s2) {
        return new SimpleStats(
            combine(s1.valueStatsMap, s2.valueStatsMap),
            combine(s1.timeStatsMap,  s2.timeStatsMap)
        );
    }
    
    private static Map<String, SimpleStatisticalSummary> combine(Map<String, SimpleStatisticalSummary> m1, Map<String, SimpleStatisticalSummary> m2) {
        Set<String> statsNames = new HashSet<String>(m1.keySet());
        statsNames.addAll(m2.keySet());
        
        Map<String, SimpleStatisticalSummary> result = new HashMap<String, SimpleStatisticalSummary>();
        
        for (String statsName : statsNames) {
            result.put(statsName, combine(m1.get(statsName), m2.get(statsName)));
        }
        
        return result;
    }
    
    private static SimpleStatisticalSummary combine(SimpleStatisticalSummary s1, SimpleStatisticalSummary s2) {
        if (s1 == null) {
            return s2;
        } else if (s2 == null) {
            return s1;
        } else {
            return SimpleStatisticalSummary.combine(Arrays.asList(s1, s2));
        }
    }
}
