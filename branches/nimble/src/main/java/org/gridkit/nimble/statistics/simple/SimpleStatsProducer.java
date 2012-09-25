package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.gridkit.nimble.statistics.StatsProducer;

public class SimpleStatsProducer implements StatsProducer<SimpleStats> {    
    public static final String STATS_NAME_ATTR = "name";

    private Map<String, SummaryStatistics> valStats;

    public SimpleStatsProducer() {
        this.valStats = new HashMap<String, SummaryStatistics>();
    }
    
    @Override
    public void report(Map<String, Object> stats) {
        Map<String, Number> report = new HashMap<String, Number>();
        
        if (stats.containsKey(STATS_NAME_ATTR)) {
            String statistica = (String)stats.get(STATS_NAME_ATTR);
            
            for (Map.Entry<String, Object> value : stats.entrySet()) {
                if (STATS_NAME_ATTR.equals(value.getKey())) {
                    continue;
                }
                
                if (!(value.getValue() instanceof Number)) {
                    continue;
                }
                
                report.put(SimpleStats.mark(statistica, value.getKey()), (Number)value.getValue());
            }
        } else {
            for (Map.Entry<String, Object> value : stats.entrySet()) {
                if (value.getValue() instanceof Number) {
                    report.put(value.getKey(), (Number)value.getValue());
                }
            }
        }
        
        reportInternal(report);
    }
    
    private void reportInternal(Map<String, Number> stats) {
        for (Map.Entry<String, Number> entry : stats.entrySet()) {
            getValStats(entry.getKey()).addValue(entry.getValue().doubleValue());
        }
    }
    
    private SummaryStatistics getValStats(String name) {
        SummaryStatistics stats = valStats.get(name);
        
        if (stats == null) {
            stats = new SummaryStatistics();
            valStats.put(name, stats);
        }
        
        return stats;
    }

    @Override
    public SimpleStats produce() {
        Map<String, StatisticalSummary> result = new HashMap<String, StatisticalSummary>();
        
        for (Map.Entry<String, SummaryStatistics> entry : valStats.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getSummary());
        }
        
        return new SimpleStats(result);
    }
}
