package org.gridkit.nimble.statistics.simple;

import java.util.Collection;

@SuppressWarnings("serial")
public class LatencyStat extends ThroughputStat {
    public double mean;
    
    public double std;
    public double var;
    
    public double min;
    public double max;
    
    public void merge(LatencyStat stat) {
        long count = (this.count + stat.count);
        
        double mean = (this.count * this.mean + stat.count * stat.mean) / count;
        
        double thisDiff = (mean - this.mean) * (mean - this.mean);
        double statDiff = (mean - stat.mean) * (mean - stat.mean);
        
        this.var = (this.count * (this.var + thisDiff) + stat.count * (stat.var + statDiff)) / count;
        this.std = Math.sqrt(var);
        
        this.mean = mean;
        
        this.min = Math.min(this.min, stat.min);
        this.max = Math.max(this.max, stat.max);
        
        super.merge(stat);
    }
    
    public static LatencyStat merge(Collection<LatencyStat> stats) {
        if (stats.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        LatencyStat result = new LatencyStat();
        
        result.count = 0;
        
        result.startTime = Long.MAX_VALUE;
        result.finishTime = Long.MIN_VALUE;
        
        result.mean = 0;
        
        result.std = 0;
        result.var = 0;
        
        result.max = Long.MIN_VALUE;
        result.min = Long.MAX_VALUE;
        
        for (LatencyStat stat : stats) {
            result.merge(stat);
        }
        
        return result;
    }
}
