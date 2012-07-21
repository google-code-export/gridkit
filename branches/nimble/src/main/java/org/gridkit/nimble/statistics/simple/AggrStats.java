package org.gridkit.nimble.statistics.simple;

import java.util.Collection;
import java.util.Map;

public class AggrStats {
    public Map<String, ThroughputStat> throughputStat;
    
    public Map<String, LatencyStat> latencyStat;
    
    public static AggrStats merge(Collection<AggrStats> stats) {
        return null;
    }
}
