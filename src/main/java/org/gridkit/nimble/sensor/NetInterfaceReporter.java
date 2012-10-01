package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.gridkit.nimble.sensor.NetInterfaceSensor.InterfaceMeasure;
import org.gridkit.nimble.statistics.StatsReporter;

@SuppressWarnings("serial")
public class NetInterfaceReporter implements Sensor.Reporter<List<NetInterfaceSensor.InterfaceMeasure>>, Serializable {
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
    public void report(List<InterfaceMeasure> measures) {
        long totalSent = 0;
        long totalReceived = 0;
        
        long minLeftTs = Long.MAX_VALUE;
        long minRightTs = Long.MAX_VALUE;
        
        for (InterfaceMeasure measure : measures) {
            long sent = measure.getRightState().getTxBytes() - measure.getLeftState().getTxBytes();
            long received = measure.getRightState().getRxBytes() -  measure.getLeftState().getRxBytes();
            
            totalSent += sent;
            totalReceived += received;
            
            minLeftTs = Math.min(minLeftTs, measure.getLeftTsNs());
            minRightTs = Math.min(minRightTs, measure.getRightTsNs());
            
            long timeNs = measure.getRightTsNs() - measure.getLeftTsNs();
            
            if (isReported(measure.getInterfaceName())) {
                reporter.report(getSentBytesStatsName(measure.getInterfaceName()), sent, timeNs);
                reporter.report(getReceivedBytesStatsName(measure.getInterfaceName()), received, timeNs);
            }
         }
         
         long totalTime = minRightTs - minLeftTs;
        
         reporter.report(getSentBytesStatsName(TOTAL_INTERFACE), totalSent, totalTime);
         reporter.report(getReceivedBytesStatsName(TOTAL_INTERFACE), totalReceived, totalTime);
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
