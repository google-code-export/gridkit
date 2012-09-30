package org.gridkit.nimble.sensor;

import static org.gridkit.nimble.util.StringOps.F;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.sigar.SigarFactory;
import org.gridkit.nimble.statistics.FlushableStatsReporter;
import org.gridkit.nimble.util.JvmOps;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings({ "serial", "restriction" })
public class CpuDemon implements Callable<Void>, Serializable {
    private static final Logger log = LoggerFactory.getLogger(CpuDemon.class);
    
    public static String TOTAL_SUFFIX = ".TOT";
    public static String SYSTEM_SUFFIX = ".SYS";
    public static String USER_SUFFIX = ".USR";
    
    private static long MIN_SLEEP_TIME_MS = 10;
    
    private String statistica;
    private CpuReporter cpuReporter;
    private FlushableStatsReporter statsReporter;
    
    private long readoutTimeoutMs;
    private long flushTimeoutMs;
    
    public static class CpuReport {
        long usr = 0;
        long sys = 0;
        long tot = 0;
        
        public CpuReport add(ProcCpu procCpu) {
            usr += procCpu.getUser();
            sys += procCpu.getSys();
            tot += procCpu.getTotal();
            return this;
        }
    }
    
    public static interface CpuReporter extends Serializable {
        CpuReport getCpuReport();
    }
    
    public CpuDemon(String statistica, CpuReporter cpuReporter, FlushableStatsReporter statsReporter,
                    long readoutTimeout, long flushTimeout, TimeUnit unit) {
        this.statistica = statistica;
        this.cpuReporter = cpuReporter;
        this.statsReporter = statsReporter;
        this.readoutTimeoutMs = Math.max(unit.toMillis(readoutTimeout), MIN_SLEEP_TIME_MS);
        this.flushTimeoutMs = Math.max(unit.toMillis(flushTimeout), MIN_SLEEP_TIME_MS);
    }

    public CpuDemon(String statistica, CpuReporter cpuReporter, FlushableStatsReporter statsReporter, long readoutTimeoutMs, long flushTimeoutMs) {
        this(statistica, cpuReporter, statsReporter, readoutTimeoutMs, flushTimeoutMs, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public Void call() {
        try {
            return callIntegnal();
        } catch (Throwable e) {
            if (!(e instanceof InterruptedException)) {
                log.error("", e);
            }
            return null;
        }
    }

    public Void callIntegnal() throws Exception {          
        long flushDelayMs = 0;
        
        long ts11 = System.nanoTime();
        CpuReport cpu1 = cpuReporter.getCpuReport();
        long ts12 = System.nanoTime();
        
        while (!Thread.interrupted()) {
            Thread.sleep(readoutTimeoutMs);
            
            long ts21 = System.nanoTime();
            CpuReport cpu2 = cpuReporter.getCpuReport();
            long ts22 = System.nanoTime();

            long timeNs = (ts22 + ts21)/2 - (ts11 + ts12)/2;
            
            report(cpu1, cpu2, timeNs);
            
            long ts3 = System.nanoTime();
            
            flushDelayMs += TimeUnit.NANOSECONDS.toMillis(ts3 - ts11);
            
            if (flushDelayMs > flushTimeoutMs) {
                statsReporter.flush();
                flushDelayMs = 0;
            }
            
            ts11 = ts21;
            cpu1 = cpu2;
            ts12 = ts22;
        }
        
        return null;
    }
    
    private void report(CpuReport cpu1, CpuReport cpu2, long timeNs) {
        if (cpu1 != null && cpu2 != null) {
            double usrTime = TimeUnit.MILLISECONDS.toNanos(cpu2.usr - cpu1.usr);
            double sysTime = TimeUnit.MILLISECONDS.toNanos(cpu2.sys - cpu1.sys);
            double totTime = TimeUnit.MILLISECONDS.toNanos(cpu2.tot - cpu1.tot);
            
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
    
    public static abstract class SigarCpuReporter implements CpuReporter, Serializable {
        private transient Sigar sigar;
        
        protected Sigar getSigar() {
            if (sigar == null) {
                sigar = SigarFactory.newSigar();
            }
            return sigar;
        }
    }
    
    public static class CurPidCpuReporter extends SigarCpuReporter {
        @Override
        public CpuReport getCpuReport() {
            try {
                return (new CpuReport()).add(getSigar().getProcCpu(getSigar().getPid()));
            } catch (SigarException e) {
                log.error("Error while getting current process CPU usage", e);
                return null;
            }
        }
    }
    
    public static abstract class PidsCpuReporter extends SigarCpuReporter {
        private Collection<Long> pids;
        private boolean refreshPids;

        public PidsCpuReporter(boolean refreshPids) {
            this.pids = null;
            this.refreshPids = refreshPids;
        }

        @Override
        public CpuReport getCpuReport() {
            CpuReport report = new CpuReport();

            if (pids == null || refreshPids) {
                pids = getPids();
            }
                        
            boolean found = false;
            
            for (long pid : pids) {
                try {
                    report.add(getSigar().getProcCpu(pid));
                    found = true;
                } catch (SigarException e) {
                    log.error(F("Error while getting processes CPU usage for pid '%d'", pid), e);
                    pids.remove(pid);
                }
            }
            
            return found ? report : null;
        }
        
        protected abstract Collection<Long> getPids();
    }
    
    /**
     * http://support.hyperic.com/display/SIGAR/PTQL
     */
    public static class PtqlCpuReporter extends PidsCpuReporter {
        private String query;

        public PtqlCpuReporter(String query) {
            super(true);
            this.query = query;
        }

        @Override
        protected Collection<Long> getPids() {
            List<Long> pids = new ArrayList<Long>();
            
            try {
                for (long pid : ProcessFinder.find(getSigar(), query)) {
                    pids.add(pid);
                }
            } catch (SigarException e) {
                log.error(F("Error while getting processes CPU usage by query '%s'", query), e);
                return null;
            }

            return pids;
        }
    }
    
    public static class JavaSysProperyCpuReporter extends PidsCpuReporter {
        private String key;
        private String value;

        public JavaSysProperyCpuReporter(String key, String value) {
            super(false);
            this.key = key;
            this.value = value;
        }

        @Override
        protected Collection<Long> getPids() {
            Collection<Long> pids = new HashSet<Long>();
                        
            for (Map.Entry<VirtualMachineDescriptor, VirtualMachine> vm : JvmOps.listVms().entrySet()) {
                try {
                    Properties props = vm.getValue().getSystemProperties();
                    
                    if (value.equals(props.getProperty(key))) {
                        pids.add(Long.valueOf(vm.getKey().id()));
                    }
                } catch (IOException e) {
                    log.error("Failed to retrieve JVM properties " + vm.getKey(), e);
                    continue;
                }
            }
            
            return pids;
        }
    }
}
