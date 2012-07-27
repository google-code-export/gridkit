package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridkit.nimble.statistics.StatsProducer;

public class SimpleStatsProducer implements StatsProducer<SimpleStats> {
    private static final Logger log = LoggerFactory.getLogger(SimpleStatsProducer.class);
    
    private Map<String, SummaryStatistics> valueStats;
    private Map<String, SummaryStatistics> timeStats;

    public SimpleStatsProducer() {
        this.valueStats = new HashMap<String, SummaryStatistics>();
        this.timeStats = new HashMap<String, SummaryStatistics>();
    }
    
    @Override
    public void report(String statistica, double value) {
        SummaryStatistics valueStat = valueStats.get(statistica);
        
        if (valueStat == null) {
            valueStat = new SummaryStatistics();
            valueStats.put(statistica, valueStat);
        }
        
        valueStat.addValue(value);
    }

    @Override
    public void report(String statistica, long timestamp, double value) {
        SummaryStatistics timeStat = timeStats.get(statistica);

        if (timeStat == null) {
            timeStat = new SummaryStatistics();
            timeStats.put(statistica, timeStat);
        }
        
        timeStat.addValue(timestamp);
        report(statistica, value);
    }

    @Override
    public void report(String message, long timestamp, Throwable throwable) {
        // TODO implement
        log.error(message, throwable);
    }
    
    @Override
    public SimpleStats produce() {
        return new SimpleStats(produce(valueStats), produce(timeStats));
    }
    
    private static Map<String, StatisticalSummary> produce(Map<String, SummaryStatistics> stats) {
        Map<String, StatisticalSummary> result = new HashMap<String, StatisticalSummary>();
        
        for (Map.Entry<String, SummaryStatistics> entry : stats.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getSummary());
        }
        
        return result;
    }
}
