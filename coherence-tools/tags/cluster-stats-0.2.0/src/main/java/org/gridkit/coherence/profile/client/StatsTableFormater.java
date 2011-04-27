/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gridkit.coherence.profile.StatValue;



/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class StatsTableFormater {

    public static String formatStatsTable(Map<String, StatValue> stats) { List<String> nameList = new ArrayList<String>(stats.keySet());
        String[] names = nameList.toArray(new String[nameList.size()]);
        StatValue[] counters = new StatValue[names.length];
        for (int i = 0; i != names.length; ++i) {
            counters[i] = stats.get(names[i]);
        }
        return StatsTableFormater.formatStatsTable(names, counters);
    }
    
    public static String formatStatsTable(String sampleNames[], StatValue[] counters) {
        StringBuilder report = new StringBuilder();

        int width = 0;
        for(String sampleName: sampleNames) {
            width = width > sampleName.length() ? width : sampleName.length();
        }
        
        report.append('\n');
        report.append(align("Counter", width));
        report.append("\t").append(align("Count", 8));
        report.append("\t").append(align("     Avg", 14));
        report.append("\t").append(align("     Total", 14));
        report.append("\t").append(align("     StdDev", 14));
        report.append('\n');

        for(int i = 0; i != sampleNames.length; ++i) {
            String sampleName = sampleNames[i];
            StatValue counter = counters[i];
            report.append(align(sampleName, width));
            report.append("\t").append(align(String.valueOf(counter.getCount()), 8));
            report.append("\t").append(align(String.format("%1$14.6f", counter.getAvg()), 14));
            report.append("\t").append(align(String.format("%1$14.6f", counter.getTotal()), 14));
            report.append("\t").append(align(String.format("%1$14.6f", counter.getStdDev()), 14));
            report.append('\n');
        }

        return report.toString();
    }

    private static String align(String sampleName, int width) {
        StringBuffer buf = new StringBuffer();
        buf.append(sampleName);
        while(buf.length() < width) {
            buf.append(' ');
        }
        return buf.toString();
    }
}
