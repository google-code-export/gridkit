package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.nimble.statistics.StatsReporter;
import org.hyperic.sigar.ProcCpu;

@SuppressWarnings("serial")
public class ProcCpuReporter implements Sensor.Reporter<List<IntervalMeasure<ProcCpu>>>, Serializable {
    public static String TOTAL_SUFFIX  = ".TOT";
    public static String USER_SUFFIX   = ".USR";
    public static String SYSTEM_SUFFIX = ".SYS";
    public static String COUNT_SUFFIX  = ".CNT";

    private String statistica;
    private StatsReporter statsReporter;

    public ProcCpuReporter(String statistica, StatsReporter statsReporter) {
        this.statistica = statistica;
        this.statsReporter = statsReporter;
    }

    @Override
    public void report(List<IntervalMeasure<ProcCpu>> measures) {
        double usr = 0.0;
        double sys = 0.0;
        double tot = 0.0;
        int    cnt = 0;
        
        for (IntervalMeasure<ProcCpu> measure : measures) {
            usr += getCpuUsage(measure.getLeftState().getUser(),  measure.getRightState().getUser(),  measure);
            sys += getCpuUsage(measure.getLeftState().getSys(),   measure.getRightState().getSys(),   measure);
            tot += getCpuUsage(measure.getLeftState().getTotal(), measure.getRightState().getTotal(), measure);
            cnt += 1;
        }
        
        Map<String, Object> sample = new HashMap<String, Object>();
        
        sample.put(getUsrStatsName(statistica), usr);
        sample.put(getSysStatsName(statistica), sys);
        sample.put(getTotStatsName(statistica), tot);
        sample.put(getCntStatsName(statistica), cnt);
            
        statsReporter.report(sample);
    }

    private static double getCpuUsage(long leftCpuMs, long rightCpuMs, IntervalMeasure<?> measure) {
        double timeNs = TimeUnit.MILLISECONDS.toNanos(rightCpuMs - leftCpuMs);
        return timeNs / (measure.getRightTsNs() - measure.getLeftTsNs());
    }
    
    public static String getTotStatsName(String statistica) {
        return statistica + TOTAL_SUFFIX;
    }
    
    public static String getSysStatsName(String statistica) {
        return statistica + SYSTEM_SUFFIX;
    }
    
    public static String getUsrStatsName(String statistica) {
        return statistica + USER_SUFFIX;
    }
    
    public static String getCntStatsName(String statistica) {
        return statistica + COUNT_SUFFIX;
    }
}
