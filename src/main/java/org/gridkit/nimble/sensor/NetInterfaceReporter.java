package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.gridkit.nimble.statistics.StatsReporter;
import org.gridkit.nimble.util.SetOps;
import org.hyperic.sigar.NetInterfaceStat;

@SuppressWarnings("serial")
public class NetInterfaceReporter implements Sensor.Reporter<Map<String, NetInterfaceStat>>, Serializable {
    public static String SENT_BYTES_SUFFIX = ".SENT";
    public static String RECEIVED_BYTES_SUFFIX = ".RECEIVED";
    
    public static String TOTAL_INTERFACE = "TOTAL";
    
    private SensorReporter reporter;
    private Set<String> interfaces;
    
    public NetInterfaceReporter(StatsReporter reporter, Set<String> interfaces) {
        this.reporter = new SensorReporter(reporter);
        this.interfaces = interfaces;
    }
    
    public NetInterfaceReporter(StatsReporter reporter) {
        this(reporter, null);
    }

    @Override
    public void report(Map<String, NetInterfaceStat> m1, Map<String, NetInterfaceStat> m2, long timeNs) {
        long totalSent = 0;
        long totalReceived = 0;
        
        for (String inter : SetOps.intersection(m1.keySet(), m2.keySet())) {
           long sent = m2.get(inter).getTxBytes() - m1.get(inter).getTxBytes();
           long received = m2.get(inter).getRxBytes() - m1.get(inter).getRxBytes();
           
           totalSent += sent;
           totalReceived += received;
           
           if (isReported(inter)) {
               reporter.report(getSentBytesStatsName(inter), sent, timeNs);
               reporter.report(getReceivedBytesStatsName(inter), received, timeNs);
           }
        }
        
        reporter.report(getSentBytesStatsName(TOTAL_INTERFACE), totalSent, timeNs);
        reporter.report(getReceivedBytesStatsName(TOTAL_INTERFACE), totalReceived, timeNs);

    }
    
    private boolean isReported(String inter) {
        return interfaces == null ? true : interfaces.contains(inter);
    }
    
    public static String getSentBytesStatsName(String inter) {
        return inter.toUpperCase() + SENT_BYTES_SUFFIX;
    }
    
    public static String getReceivedBytesStatsName(String inter) {
        return inter.toUpperCase() + RECEIVED_BYTES_SUFFIX;
    }
}
