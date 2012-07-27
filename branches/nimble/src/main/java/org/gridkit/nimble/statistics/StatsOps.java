package org.gridkit.nimble.statistics;

import java.io.PrintStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.gridkit.nimble.statistics.simple.SimpleStats;
import org.gridkit.nimble.util.ValidOps;

public class StatsOps {
    private static final Map<TimeUnit, String> timeAlias = new HashMap<TimeUnit, String>();
    
    private static final String ENDL = "\n";
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    
    private static final StatisticalSummary emptySummary = (new SummaryStatistics()).getSummary();
    
    static {
        timeAlias.put(TimeUnit.NANOSECONDS,  "ns");
        timeAlias.put(TimeUnit.MICROSECONDS, "us");
        timeAlias.put(TimeUnit.MILLISECONDS, "ms");
        timeAlias.put(TimeUnit.SECONDS,      "s");
        timeAlias.put(TimeUnit.HOURS,        "h");
        timeAlias.put(TimeUnit.DAYS,         "d");
    }
    
    public static String getTimeAlias(TimeUnit timeUnit) {
        return timeAlias.get(timeUnit);
    }
    
    public static String latencyToString(StatisticalSummary stats, TimeUnit timeUnit) {
        String alias = " " + getTimeAlias(timeUnit);
        
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
        String alias = getTimeAlias(timeUnit);

        String startTime = dateFormat.format(new Date((long)stats.getMin()));
        String finishTime = dateFormat.format(new Date((long)stats.getMax()));
        
        StringBuilder outBuffer = new StringBuilder();
        
        outBuffer.append("count:      ").append(stats.getN())                                               .append(ENDL);
        outBuffer.append("throughput: ").append(stats.getThroughput(timeUnit)).append(" ops/").append(alias).append(ENDL);
        outBuffer.append("duration:   ").append(stats.getDuration(timeUnit))  .append(" ")    .append(alias).append(ENDL);
        outBuffer.append("start       ").append(startTime)                                                  .append(ENDL);
        outBuffer.append("finish      ").append(finishTime);
        
        return outBuffer.toString();
    }
    
    public static void printTimeSummary(PrintStream stream, Collection<String> statsNames, SimpleStats simpleStats, TimeUnit latencyUnit, TimeUnit throughputUnit) {
        ValidOps.notEmpty(statsNames, "statsNames");
        
        String ltAlias = " (" + getTimeAlias(latencyUnit) + ")";
        String varAlias = " (" + getTimeAlias(latencyUnit) + "^2)";
        
        String thAlias = " (ops/" + getTimeAlias(throughputUnit) + ")";
        String durAlias = " (" + getTimeAlias(throughputUnit) + ")";

        List<List<String>> rows = new ArrayList<List<String>>();
        
        rows.add(Arrays.asList(
            "Name", "N", "Mean" + ltAlias,
            "Var" + varAlias, "Sd" + ltAlias,
            "Min" + ltAlias, "Max" + ltAlias,
            "Th" + thAlias, "Dur" + durAlias
        ));
        
        for (String statsName : statsNames) {
            StatisticalSummary ltStats = simpleStats.getLatency(statsName, latencyUnit);
            ThroughputSummary thStats = simpleStats.getThroughput(statsName);
            
            if (ltStats == null || thStats == null) {
                continue;
            }
            
            rows.add(Arrays.asList(
                statsName, String.valueOf(ltStats.getN()), decimalFormat.format(ltStats.getMean()),
                decimalFormat.format(ltStats.getVariance()), decimalFormat.format(ltStats.getStandardDeviation()),
                decimalFormat.format(ltStats.getMin()), decimalFormat.format(ltStats.getMax()),
                decimalFormat.format(thStats.getThroughput(throughputUnit)), decimalFormat.format(thStats.getDuration(throughputUnit))
            ));
        }
        
        printTable(stream, rows);
    }
    
    public static void printValueSummary(PrintStream stream, Collection<String> statsNames, SimpleStats simpleStats) {
        ValidOps.notEmpty(statsNames, "statsNames");

        List<List<String>> rows = new ArrayList<List<String>>();
        
        rows.add(Arrays.asList("Name", "N", "Mean", "Var", "Sd", "Min", "Max"));
        
        for (String statsName : statsNames) {
            StatisticalSummary stats = simpleStats.getValueStats(statsName);
            
            if (stats == null) {
                continue;
            }
            
            rows.add(Arrays.asList(
                statsName, String.valueOf(stats.getN()), decimalFormat.format(stats.getMean()),
                decimalFormat.format(stats.getVariance()), decimalFormat.format(stats.getStandardDeviation()),
                decimalFormat.format(stats.getMin()), decimalFormat.format(stats.getMax())
            ));
        }
        
        printTable(stream, rows);
    }
    
    private static void printTable(PrintStream stream, List<List<String>> rows) {
        List<Integer> lens = columnLens(rows);
        
        for (List<String> row : rows) {
            stream.print("| ");
            
            for (int c = 0; c < lens.size(); ++c) {
                String cell = c < row.size() ? row.get(c) : "";
                stream.printf("%" + lens.get(c) + "s | ", cell);
            }
            
            stream.println();
        }
    }
    
    private static List<Integer> columnLens(List<List<String>> rows) {
        int maxSize = maxSize(rows);
        
        List<Integer> lens = new ArrayList<Integer>(maxSize);
        
        for (int c = 0; c < maxSize; ++c) {
            int len = Integer.MIN_VALUE;
            
            for (int r = 0; r < rows.size(); ++r) {
                List<String> row = rows.get(r);
                
                if (c < row.size()) {
                    len = Math.max(len, row.get(c).length());
                }
            }
            
            lens.add(len);
        }
        
        return lens;
    }
    
    private static int maxSize(List<List<String>> rows) {
        int max = Integer.MIN_VALUE;
        
        for (List<String> list : rows) {
            max = Math.max(max, list.size());
        }
        
        return max;
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
