package org.gridkit.nimble.statistics.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.statistics.ThroughputSummary;
import org.gridkit.nimble.util.ValidOps;

public abstract class SimplePrinter {
    private static final Map<TimeUnit, String> timeAliasMap = new HashMap<TimeUnit, String>();

    private static final int MISSED_GEN  = 1; // n
    private static final int MISSED_LT   = 4; // mean, sd, min, max
    private static final int MISSED_TH   = 2; // th, dur
    private static final int MISSED_VAL  = 5; // n, mean, sd, min, max
    
    static {
        timeAliasMap.put(TimeUnit.NANOSECONDS,  "ns");
        timeAliasMap.put(TimeUnit.MICROSECONDS, "us");
        timeAliasMap.put(TimeUnit.MILLISECONDS, "ms");
        timeAliasMap.put(TimeUnit.SECONDS,      "s");
        timeAliasMap.put(TimeUnit.HOURS,        "h");
        timeAliasMap.put(TimeUnit.DAYS,         "d");
    }

    private final List<String> leftHeader;
    private final List<String> rightHeader;
    
    private DecimalFormat decFormat = new DecimalFormat("0.00");
    
    private TimeUnit ltUnit = TimeUnit.MILLISECONDS;
    private TimeUnit thUnit = TimeUnit.SECONDS;

    public SimplePrinter(List<String> leftHeader, List<String> rightHeader) {
        ValidOps.notNull(leftHeader, "leftHeader");
        ValidOps.notNull(rightHeader, "rightHeader");

        this.leftHeader = leftHeader;
        this.rightHeader = rightHeader;;
    }

    // ------------------------------------------------------------------------------------- //
    
    public void print(PrintStream stream, SimpleStats stats) {
        print(stream, stats, Collections.<String>emptyList(), Collections.<String>emptyList());
    }
    
    public void print(File file, SimpleStats stats) throws IOException {
        print(file, stats, Collections.<String>emptyList(), Collections.<String>emptyList());
    }
    
    public void print(PrintStream stream, SimpleStats stats, List<String> leftValues, List<String> rightValues) {
        print(stream, stats, stats.getValStatsNames(SimpleStats.TIME_NS_MARK), leftValues, rightValues);
    }
    
    public void print(File file, SimpleStats stats, List<String> leftValues, List<String> rightValues) throws IOException {
        print(file, stats, stats.getValStatsNames(SimpleStats.TIME_NS_MARK), leftValues, rightValues);
    }
    
    public void print(PrintStream stream, SimpleStats stats, Collection<String> statsNames,
                      List<String> leftValues, List<String> rightValues) {
        List<String> header = newTimeHeader(ltUnit, thUnit);
        List<List<String>> table = newTimeTable(stats, statsNames, leftValues, rightValues);
        
        table.add(0, header);
        
        printTable(stream, table);
    }
    
    public void print(File file, SimpleStats stats, Collection<String> statsNames,
                      List<String> leftValues, List<String> rightValues) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(file, true);
        PrintStream printStream = new PrintStream(fileStream);

        try {
            if (file.length() == 0) {
                List<String> header = newTimeHeader(ltUnit, thUnit);
                printTable(printStream, Collections.singletonList(header));
            }
            
            List<List<String>> table = newTimeTable(stats, statsNames, leftValues, rightValues);
            
            printTable(printStream, table);
        } finally {
            printStream.close();
        }
    }
    
    // ------------------------------------------------------------------------------------- //
    
    public void printValues(PrintStream stream, SimpleStats stats, Collection<String> statsNames,
                            List<String> leftValues, List<String> rightValues) { 
        List<String> header = newValueHeader(TimeUnit.SECONDS);
        List<List<String>> table = newValueTable(stats, statsNames, leftValues, rightValues);
        
        table.add(0, header);

        printTable(stream, table);
    }
    
    // ------------------------------------------------------------------------------------- //
    
    protected abstract void printTable(PrintStream stream, List<List<String>> table);
    
    protected List<List<String>> newTimeTable(SimpleStats stats, Collection<String> statsNames,
                                              List<String> leftValues, List<String> rightValues) {
        List<List<String>> table = new ArrayList<List<String>>();
        
        leftValues = adjustLength(leftValues, leftHeader.size());
        rightValues = adjustLength(rightValues, rightHeader.size());
        
        for (String statsName : new TreeSet<String>(statsNames)) {
            List<String> row = new ArrayList<String>();
            table.add(row);
            
            row.addAll(leftValues);
            row.add(statsName);
            
            StatisticalSummary ltStats = stats.getLatency(statsName, ltUnit);
            ThroughputSummary thStats = stats.getThroughput(statsName);
            
            StatisticalSummary genStats = (ltStats != null ? ltStats : thStats);
            
            if (genStats == null) {
                addMissed(row, MISSED_GEN + MISSED_LT + MISSED_TH);
            } else {
                row.add(String.valueOf(genStats.getN()));
                
                if (ltUnit != null) {                
                    if (ltStats != null) {
                        row.add(decFormat.format(ltStats.getMean()));
                        row.add(decFormat.format(ltStats.getStandardDeviation()));
                        //row.add(decFormat.format(ltStats.getVariance()));
                        row.add(decFormat.format(ltStats.getMin()));
                        row.add(decFormat.format(ltStats.getMax()));
                    } else {
                        addMissed(row, MISSED_LT);
                    }
                }
                
                if (thUnit != null) {
                    if (thStats != null) {
                        row.add(decFormat.format(thStats.getThroughput(thUnit)));
                        row.add(decFormat.format(thStats.getDuration(thUnit)));
                    } else {
                        addMissed(row, MISSED_TH);
                    }
                }
            }
            
            row.addAll(rightValues);
        }
        
        return table;
    }
    
    protected List<List<String>> newValueTable(SimpleStats stats, Collection<String> statsNames,
                                               List<String> leftValues, List<String> rightValues) {
        List<List<String>> table = new ArrayList<List<String>>();
        
        leftValues = adjustLength(leftValues, leftHeader.size());
        rightValues = adjustLength(rightValues, rightHeader.size());
        
        for (String statsName : statsNames) {
            List<String> row = new ArrayList<String>();
            table.add(row);
            
            row.addAll(leftValues);
            row.add(statsName);

            StatisticalSummary valStats = stats.getValStats(statsName);
            
            if (valStats != null) {
                row.add(String.valueOf(valStats.getN()));
                row.add(decFormat.format(valStats.getMean()));
                row.add(decFormat.format(valStats.getStandardDeviation()));
                //row.add(decFormat.format(valStats.getVariance()));
                row.add(decFormat.format(valStats.getMin()));
                row.add(decFormat.format(valStats.getMax()));
            } else {
                addMissed(row, MISSED_VAL);
            }
            
            row.addAll(rightValues);
        }
        
        return table;
    }
    
    private static void addMissed(List<String> row, int count) {
        while (count-- > 0){
            row.add("");
        }
    }
    
    protected List<String> newTimeHeader(TimeUnit latencyUnit, TimeUnit throughputUnit) {
        List<String> result = new ArrayList<String>();
        
        result.addAll(leftHeader);
        result.add("Statistica");
        result.add("N");

        if (latencyUnit != null) {
            String ltAlias = " (" + getTimeAlias(latencyUnit) + ")";
            //String varAlias = " (" + getTimeAlias(latencyUnit) + "^2)";

            result.add("Mean" + ltAlias);
            result.add("Sd"   + ltAlias);
            //result.add("Var"  + varAlias);
            result.add("Min"  + ltAlias);
            result.add("Max"  + ltAlias);
        }
        
        if (throughputUnit != null) {
            String thAlias = " (ops/" + getTimeAlias(throughputUnit) + ")";
            String durAlias = " (" + getTimeAlias(throughputUnit) + ")";

            result.add("Th"  + thAlias);
            result.add("Dur" + durAlias);
        }

        result.addAll(rightHeader);
        
        return result;
    }
    
    protected List<String> newValueHeader(TimeUnit throughputUnit) {
        List<String> result = new ArrayList<String>();
        
        result.addAll(leftHeader);
        result.addAll(Arrays.asList("Name", "N", "Mean", "Sd", "Min", "Max"));
        result.addAll(rightHeader);

        return result;
    }
    
    protected String getTimeAlias(TimeUnit timeUnit) {
        return timeAliasMap.get(timeUnit);
    }
    
    private static List<String> adjustLength(List<String> list, int len) {
        if (list.size() < len) {
            List<String> result = new ArrayList<String>(len);
            
            result.addAll(list);
            while (result.size() < len) {
                result.add("");
            }
            
            return result;
        } else if (list.size() == len) {
            return list;
        } else {
            return list.subList(0, len);
        }
    }
}
