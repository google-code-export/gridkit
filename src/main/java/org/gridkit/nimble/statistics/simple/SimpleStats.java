package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class SimpleStats {
    private final Map<String, SummaryStatistics> valueStats;
    private final Map<String, SummaryStatistics> timeStats;

    public SimpleStats() {
        this(new HashMap<String, SummaryStatistics>(), new HashMap<String, SummaryStatistics>());
    }
    
    public SimpleStats(Map<String, SummaryStatistics> valueStats, Map<String, SummaryStatistics> timeStats) {
        this.valueStats = valueStats;
        this.timeStats = timeStats;
    }
    
    public SummaryStatistics getValueStat(String statistica) {
        return valueStats.get(statistica);
    }
    
    public SummaryStatistics getTimeStat(String statistica) {
        return timeStats.get(statistica);
    }

    public Map<String, SummaryStatistics> getValueStats() {
        return valueStats;
    }

    public Map<String, SummaryStatistics> getTimeStats() {
        return timeStats;
    }
}
