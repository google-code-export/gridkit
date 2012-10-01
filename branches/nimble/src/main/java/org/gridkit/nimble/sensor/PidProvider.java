package org.gridkit.nimble.sensor;

import static org.gridkit.nimble.util.StringOps.F;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.gridkit.nimble.util.JvmOps;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public interface PidProvider {
    Collection<Long> getPids();
    
    @SuppressWarnings("serial")
    public static class CurPidProvider extends SigarHolder implements PidProvider {
        @Override
        public Collection<Long> getPids() {
            return Collections.singleton(getSigar().getPid());
        }
    }

    /**
     * http://support.hyperic.com/display/SIGAR/PTQL
     */
    @SuppressWarnings("serial")
    public static class PtqlPidProvider extends SigarHolder implements PidProvider {
        private static final Logger log = LoggerFactory.getLogger(PtqlPidProvider.class);
        
        private String query;

        public PtqlPidProvider(String query) {
            this.query = query;
        }

        @Override
        public Collection<Long> getPids() {
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
    
    @SuppressWarnings("serial")
    public static class JavaPidProvider implements PidProvider, Serializable {
        private static final Logger log = LoggerFactory.getLogger(JavaPidProvider.class);
        
        private String key;
        private String value;
        private List<String> displayNames;

        //TODO add more precise filtering
        // /tmp/.gridagent/.cache/ef379967543782d3de6b0954fb92ebdda73bc340/booter.jar d1c5837e2f12ec412e933d6b44270528 localhost 50009 /tmp/.gridagent
        // org.gridkit.vicluster.telecontrol.bootstraper.Bootstraper                  d1452176ab6291598303f77f5299d13d localhost 48669
        public JavaPidProvider(String key, String value, List<String> displayNames) {
            this.key = key;
            this.value = value;
            this.displayNames = displayNames;
        }

        @Override
        public Collection<Long> getPids() {
            Collection<Long> pids = new HashSet<Long>();
            
            List<VirtualMachineDescriptor> descs = JvmOps.listVms();
            Collections.shuffle(descs);
                            
            for (VirtualMachineDescriptor desc : descs) {
                if (displayNames != null) {
                    boolean valid = false;
                    
                    for (String displayName : displayNames) {
                        if (desc.displayName() != null && desc.displayName().contains(displayName)) {
                            valid = true;
                        }
                    }
                    
                    if (!valid) {
                        continue;
                    }
                }
                
                VirtualMachine vm = null;
                
                long pid = Long.valueOf(desc.id());
                
                synchronized (JavaPidProvider.class) {
                    try {
                        vm = VirtualMachine.attach(desc);
                        
                        Properties props = vm.getSystemProperties();
                        
                        if (value.equals(props.getProperty(key))) {
                            pids.add(pid);
                        }
                    } catch (Exception e) {
                        log.error("Failed to retrieve JVM properties of " + desc, e);
                        continue;
                    } finally {
                        if (vm != null) {
                            try {
                                vm.detach();
                            } catch (IOException e) {
                                log.warn("Failed to detach from vm " + desc, e);
                            }
                        }
                    }
                }
            }
                                        
            return pids;
        }
        
        @Override
        public String toString() {
            return F("%s[%s,%s]", JavaPidProvider.class.getSimpleName(), key, value);
        }
    }
    
    @SuppressWarnings("serial")
    public static class CompositePidProvider implements PidProvider, Serializable {
        private Collection<PidProvider> pidProviders;

        public CompositePidProvider(Collection<PidProvider> pidProviders) {
            this.pidProviders = pidProviders;
        }

        public CompositePidProvider(PidProvider... pidProviders) {
            this(Arrays.asList(pidProviders));
        }

        @Override
        public Collection<Long> getPids() {
            Collection<Long> result = new HashSet<Long>();
            
            for (PidProvider pidProvider : pidProviders) {
                result.addAll(pidProvider.getPids());
            }
            
            return result;
        }
    }
}
