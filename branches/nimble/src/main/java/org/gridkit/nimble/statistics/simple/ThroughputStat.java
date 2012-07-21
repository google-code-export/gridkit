package org.gridkit.nimble.statistics.simple;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class ThroughputStat implements Serializable {
    public long count;
    
    public long startTime;
    public long finishTime;
    
    public double getThroughput(TimeUnit timeUnit) {
        double duration = getDuration(TimeUnit.NANOSECONDS);
        
        return count / (duration / timeUnit.toNanos(1));
    }
    
    public long getDuration(TimeUnit timeUnit) {
        return timeUnit.convert(finishTime - startTime, TimeUnit.MILLISECONDS);
    }
    
    public void merge(ThroughputStat stat) {
        this.count += stat.count;
        
        if (this.startTime > stat.startTime) {
            this.startTime = stat.startTime;
        }
        
        if (this.finishTime < stat.finishTime) {
            this.finishTime = stat.finishTime;
        }
    }
    
    public static ThroughputStat merge(Collection<ThroughputStat> stats) {
        if (stats.isEmpty()) {
            throw new IllegalArgumentException();
        }
        
        ThroughputStat result = new ThroughputStat();
        
        result.count = 0;
        result.startTime = Long.MAX_VALUE;
        result.finishTime = Long.MIN_VALUE;
        
        for (ThroughputStat stat : stats) {
            result.merge(stat);
        }
        
        return result;
    }
}
