package org.gridkit.nimble.sensor;

import static org.gridkit.nimble.util.StringOps.F;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class NetInterfaceSensor extends SigarHolder implements Sensor<Map<String, NetInterfaceStat>> {
    private static final Logger log = LoggerFactory.getLogger(NetInterfaceSensor.class);
    
    private static long MIN_SLEEP_TIME_S = 3;

    private long sleepTimeMs;
    private transient String[] interfaces;
    
    public NetInterfaceSensor(long sleepTime, TimeUnit unit) {
        this.sleepTimeMs = Math.max(unit.toMillis(sleepTime), TimeUnit.SECONDS.toMillis(MIN_SLEEP_TIME_S));
    }
    
    public NetInterfaceSensor(long sleepTimeS) {
        this(sleepTimeS, TimeUnit.SECONDS);
    }
    
    public NetInterfaceSensor() {
        this(MIN_SLEEP_TIME_S);
    }
    
    @Override
    public Map<String, NetInterfaceStat> measure() {
        Map<String, NetInterfaceStat> measure = new HashMap<String, NetInterfaceStat>();

        for (String inter : getInterfaces()) {
            try {
                measure.put(inter, getSigar().getNetInterfaceStat(inter));
            } catch (SigarException e) {
                log.warn(F("Failed to retrieve interface stats for '%s'", inter), e);
            }
        }
                
        return measure;
    }
    
    public String[] getInterfaces() {
        if (interfaces == null || interfaces.length == 0) {
            try {
                interfaces = getSigar().getNetInterfaceList();
            } catch (SigarException e) {
                log.warn("Failed to retrieve interfaces list", e);
                interfaces = new String[] {};
            }
        }
        
        return interfaces;
    }

    @Override
    public long getSleepTimeMs() {
        return sleepTimeMs;
    }

}
