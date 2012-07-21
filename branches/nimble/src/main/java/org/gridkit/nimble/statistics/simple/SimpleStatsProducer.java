package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import org.gridkit.nimble.statistics.StatsProducer;

public class SimpleStatsProducer implements StatsProducer<SimpleStats> {
    private Map<String, SummaryStatistics> valueStats;
    private Map<String, SummaryStatistics> timeStats;

    public SimpleStatsProducer() {
        this.timeStats = new HashMap<String, SummaryStatistics>();
        this.valueStats = new HashMap<String, SummaryStatistics>();
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
        
        report(statistica, value);
    }

    @Override
    public void report(String message, long timestamp, Throwable throwable) {
        // TODO implement
    }

    private static SimpleStats.Statistica toStatistica(StatisticalSummary summary) {
        SimpleStats.Statistica result = new SimpleStats.Statistica();
        
        result.size = summary.getN();
        
        result.mean = summary.getMean();
        
        result.std = 
        
        return result;
    }
    
    @Override
    public SimpleStats produce() {
        return null;
    }

}
