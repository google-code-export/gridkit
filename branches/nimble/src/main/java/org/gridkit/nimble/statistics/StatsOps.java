package org.gridkit.nimble.statistics;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class StatsOps {
    private static final Map<TimeUnit, String> timeAlias = new HashMap<TimeUnit, String>();
    
    private static final String ENDL = "\n";
    
    private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");

    private static final StatisticalSummary emptySummary = (new SummaryStatistics()).getSummary();
    
    static {
        timeAlias.put(TimeUnit.NANOSECONDS,  "ns");
        timeAlias.put(TimeUnit.MICROSECONDS, "us");
        timeAlias.put(TimeUnit.MILLISECONDS, "ms");
        timeAlias.put(TimeUnit.SECONDS,      "s");
        timeAlias.put(TimeUnit.HOURS,        "h");
        timeAlias.put(TimeUnit.DAYS,         "d");
    }
    
    public static String getAlias(TimeUnit timeUnit) {
        return timeAlias.get(timeUnit);
    }
    
    public static String latencyToString(StatisticalSummary stats, TimeUnit timeUnit) {
        String alias = " " + getAlias(timeUnit);
        
        StringBuilder outBuffer = new StringBuilder();
        
        outBuffer.append("n:    ").append(stats.getN())                                           .append(ENDL);
        outBuffer.append("mean: ").append(stats.getMean())             .append(alias)             .append(ENDL);
        outBuffer.append("var:  ").append(stats.getVariance())         .append(alias).append("^2").append(ENDL);
        outBuffer.append("sd:   ").append(stats.getStandardDeviation()).append(alias)             .append(ENDL);
        outBuffer.append("min:  ").append(stats.getMin())              .append(alias)             .append(ENDL);
        outBuffer.append("max:  ").append(stats.getMax())              .append(alias);
        
        return outBuffer.toString();
    }
    
    public static String throughputToString(ThroughputSummary stats, TimeUnit timeUnit) {
        String alias = getAlias(timeUnit);

        String startTime = format.format(new Date((long)stats.getMin()));
        String finishTime = format.format(new Date((long)stats.getMax()));
        
        StringBuilder outBuffer = new StringBuilder();
        
        outBuffer.append("count:      ").append(stats.getN())                                               .append(ENDL);
        outBuffer.append("throughput: ").append(stats.getThroughput(timeUnit)).append(" ops/").append(alias).append(ENDL);
        outBuffer.append("duration:   ").append(stats.getDuration(timeUnit))  .append(" ")    .append(alias).append(ENDL);
        outBuffer.append("start       ").append(startTime)                                                  .append(ENDL);
        outBuffer.append("finish      ").append(finishTime);
        
        return outBuffer.toString();
    }
    
    public static StatisticalSummary combine(StatisticalSummary s1, StatisticalSummary s2) {
        if (s1.getN() == 0){
            return s2;
        } else if (s2.getN() == 0) {
            return s1;
        } else if (s1.getN() == 0 && s2.getN() == 0) {
            return emptySummary;
        }
        
        long n = s1.getN() + s2.getN();
        
        double mean = (s1.getN() * s1.getMean() + s2.getN() * s2.getMean()) / n;
        
        double s1Diff = (mean - s1.getMean()) * (mean - s1.getMean());
        double s2Diff = (mean - s2.getMean()) * (mean - s2.getMean());
        
        double var = (s1.getN() * (s1.getVariance() + s1Diff) + s2.getN() * (s2.getVariance() + s2Diff)) / n;
        
        double sum = s1.getSum() + s2.getSum();
        
        double max = Math.max(s1.getMax(), s2.getMax());
        double min = Math.min(s1.getMin(), s2.getMin());
        
        return new StatisticalSummaryValues(mean, var, n, max, min, sum);
        
    }
}
