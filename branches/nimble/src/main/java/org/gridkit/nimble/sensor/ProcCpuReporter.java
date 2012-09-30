package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.nimble.statistics.StatsReporter;

@SuppressWarnings("serial")
public class ProcCpuReporter implements Sensor.Reporter<ProcCpu>, Serializable {
    public static String TOTAL_SUFFIX  = ".TOT";
    public static String USER_SUFFIX   = ".USR";
    public static String SYSTEM_SUFFIX = ".SYS";

    private String statistica;
    private StatsReporter statsReporter;

    public ProcCpuReporter(String statistica, StatsReporter statsReporter) {
        this.statistica = statistica;
        this.statsReporter = statsReporter;
    }

    @Override
    public void report(ProcCpu cpu1, ProcCpu cpu2, long timeNs) {
        if (cpu1 != null && cpu2 != null) {            
            double usrTime = TimeUnit.MILLISECONDS.toNanos(cpu2.getUsr() - cpu1.getUsr());
            double sysTime = TimeUnit.MILLISECONDS.toNanos(cpu2.getSys() - cpu1.getSys());
            double totTime = TimeUnit.MILLISECONDS.toNanos(cpu2.getTot() - cpu1.getTot());
            
            Map<String, Object> report = new HashMap<String, Object>();

            report.put(getTotStatsName(statistica), totTime / timeNs);
            report.put(getUsrStatsName(statistica), usrTime / timeNs);
            report.put(getSysStatsName(statistica), sysTime / timeNs);
            
            statsReporter.report(report);
        }
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
}
