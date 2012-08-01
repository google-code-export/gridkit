package org.gridkit.nimble.statistics.simple;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.ScaledStatisticalSummary;
import org.gridkit.nimble.statistics.StatsOps;
import org.gridkit.nimble.statistics.ThroughputSummary;

@SuppressWarnings("serial")
public class SimpleStats implements Serializable {
    private final Map<String, StatisticalSummary> valueStatsMap;
    private final Map<String, StatisticalSummary> timeStatsMap;

    public SimpleStats() {
        this(new HashMap<String, StatisticalSummary>(), new HashMap<String, StatisticalSummary>());
    }
    
    public SimpleStats(Map<String, StatisticalSummary> valueStatsMap, Map<String, StatisticalSummary> timeStatsMap) {
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
        StatisticalSummary vs = getValueStats(statistica);
        
        if (vs == null) {
            return vs;
        } else {
            double scale = 1.0 / TimeUnit.NANOSECONDS.convert(1, timeUnit);
            return new ScaledStatisticalSummary(vs, scale);
        }
    }
    
    public ThroughputSummary getThroughput(String statistica) {
        StatisticalSummary ts = getTimeStats(statistica);
        StatisticalSummary vs = getValueStats((SimpleThroughputSummary.getFinishStats(statistica)));

        if (ts == null || vs == null) {
            return null;
        } else {
            return new SimpleThroughputSummary(ts, vs);
        }
    }
    
    public static SimpleStats combine(SimpleStats s1, SimpleStats s2) {
        return new SimpleStats(
            combine(s1.valueStatsMap, s2.valueStatsMap),
            combine(s1.timeStatsMap,  s2.timeStatsMap)
        );
    }
    
    private static Map<String, StatisticalSummary> combine(Map<String, StatisticalSummary> m1, Map<String, StatisticalSummary> m2) {
        Set<String> statsNames = new HashSet<String>(m1.keySet());
        statsNames.addAll(m2.keySet());
        
        Map<String, StatisticalSummary> result = new HashMap<String, StatisticalSummary>();
        
        for (String statsName : statsNames) {
            result.put(statsName, combine(m1.get(statsName), m2.get(statsName)));
        }
        
        return result;
    }
    
    private static StatisticalSummary combine(StatisticalSummary s1, StatisticalSummary s2) {
        if (s1 == null) {
            return s2;
        } else if (s2 == null) {
            return s1;
        } else {
            return StatsOps.combine(s1, s2);
        }
    }
}
