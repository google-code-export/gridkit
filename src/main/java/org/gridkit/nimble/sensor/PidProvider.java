package org.gridkit.nimble.sensor;

import static org.gridkit.nimble.util.StringOps.F;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.gridkit.nimble.util.JvmOps;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings("restriction")
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

        public JavaPidProvider(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Collection<Long> getPids() {
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
