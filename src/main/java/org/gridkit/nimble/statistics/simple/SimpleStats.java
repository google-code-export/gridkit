package org.gridkit.nimble.statistics.simple;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.SmartReporter;
import org.gridkit.nimble.statistics.StatsOps;
import org.gridkit.nimble.statistics.ThroughputSummary;
import org.gridkit.nimble.util.Pair;
import org.gridkit.nimble.util.StringOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SimpleStats implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(SimpleStats.class);
        
    private final Map<String, StatisticalSummary> valStats;

    public SimpleStats() {
        this(new HashMap<String, StatisticalSummary>());
    }
    
    public SimpleStats(Map<String, StatisticalSummary> statsMap) {
        this.valStats = statsMap;
    }
    
    public StatisticalSummary getValStats(String name) {
        return valStats.get(name);
    }
    
    public StatisticalSummary getValStats(String statistica, String mark) {
        return valStats.get(SmartReporter.mark(statistica, mark));
    }
    
    public Set<String> getValStatsNames() {
        return new HashSet<String>(valStats.keySet());
    }
    
    public Set<String> getValStatsNames(String mark) {
        Set<String> result = new HashSet<String>();
        
        Iterator<String> iter = valStats.keySet().iterator();
        
        while (iter.hasNext()) {
            try {
                Pair<String, String> entry = SmartReporter.unmark(iter.next());
                
                if (mark.equals(entry.getB())) {
                    result.add(entry.getA());
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        
        return result;
    }
    
    public StatisticalSummary getLatency(String statistica, TimeUnit timeUnit) {
        StatisticalSummary vs = getValStats(statistica, SmartReporter.TIME_NS_MARK);
        
        if (vs == null) {
            return null;
        } else {
            return StatsOps.scale(vs, StatsOps.getScale(TimeUnit.NANOSECONDS, timeUnit));
        }
    }

    public ThroughputSummary getThroughput(String statistica) {
        return getThroughput(statistica, 1.0d);
    }
    
    public ThroughputSummary getThroughput(String statistica, String mark) {
        return getThroughput(statistica, mark, 1.0d);
    }
    
    public ThroughputSummary getThroughput(String statistica, double scale) {
        return getThroughput(statistica, SmartReporter.OPS_MARK, scale);
    }

    public ThroughputSummary getThroughput(String statistica, String mark, double scale) {
        if (!isThroughput(statistica, mark)) {
            return null;
        }
        
        StatisticalSummary stats = getValStats(statistica, mark);
        
        StatisticalSummary startStats = getValStats(statistica, SmartReporter.START_MS_MARK);
        StatisticalSummary finishStats = getValStats(statistica, SmartReporter.FINISH_MS_MARK);
        
        return new SimpleThroughputSummary(stats, scale, startStats.getMin(), finishStats.getMax());
    }

    private boolean isThroughput(String statistica, String mark) {
        StatisticalSummary stats = getValStats(statistica, mark);
        StatisticalSummary startStats = getValStats(statistica, SmartReporter.START_MS_MARK);
        StatisticalSummary finishStats = getValStats(statistica, SmartReporter.FINISH_MS_MARK);

        boolean result = stats != null && startStats != null && finishStats != null;
        
        if (result && (stats.getN() != startStats.getN() || stats.getN() != finishStats.getN())) {
            log.warn(StringOps.F("Different stats sizes for throughput[%s, %s]", statistica, mark));
        }
        
        return result;
    }
    
    public static SimpleStats combine(SimpleStats s1, SimpleStats s2) {
        return new SimpleStats(combine(s1.valStats, s2.valStats));
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
