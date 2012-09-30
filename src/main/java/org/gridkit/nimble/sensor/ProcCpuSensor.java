package org.gridkit.nimble.sensor;

import static org.gridkit.nimble.util.StringOps.F;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class ProcCpuSensor extends SigarHolder implements Sensor<ProcCpu> {
    private static final Logger log = LoggerFactory.getLogger(ProcCpuSensor.class);
    
    private static long MIN_SLEEP_TIME_S = 3;
    
    private PidProvider pidProvider;
    private long sleepTimeMs;
    private boolean refreshPids;
    
    private transient Collection<Long> pids;

    public ProcCpuSensor(PidProvider pidProvider, long sleepTime, TimeUnit unit, boolean refreshPids) {
        this.pidProvider = pidProvider;
        this.sleepTimeMs = Math.max(unit.toMillis(sleepTime), TimeUnit.SECONDS.toMillis(MIN_SLEEP_TIME_S));
    }
    
    public ProcCpuSensor(PidProvider pidProvider, long sleepTimeS, boolean refreshPids) {
        this(pidProvider, sleepTimeS, TimeUnit.SECONDS, refreshPids);
    }
    
    public ProcCpuSensor(PidProvider pidProvider) {
        this(pidProvider, MIN_SLEEP_TIME_S, false);
    }

    @Override
    public ProcCpu measure() {
        ProcCpu procCpu = new ProcCpu();

        if (pids == null || refreshPids) {
            pids = pidProvider.getPids();
        }
                    
        boolean found = false;
        
        for (long pid : pids) {
            try {
                procCpu.add(getSigar().getProcCpu(pid));
                found = true;
            } catch (SigarException e) {
                log.error(F("Error while getting processes CPU usage for pid '%d'", pid), e);
                pids.remove(pid);
            }
        }
        
        return found ? procCpu : null;
    }

    @Override
    public long getSleepTimeMs() {
        return sleepTimeMs;
    }
}
