package org.gridkit.nimble.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JvmOps {
    private static final Logger log = LoggerFactory.getLogger(JvmOps.class);
    
    private static LoadingCache<VirtualMachineDescriptor, Properties> vmPropsCache =
        CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build(new JvmPropsLoader());
        
    static {
        try {
            String javaHome = System.getProperty("java.home");
            String toolsJarURL = "file:" + javaHome + "/../lib/tools.jar";

            // Make addURL public
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            
            URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            method.invoke(sysloader, (Object) new URL(toolsJarURL));
        } catch (Exception e) {
            log.error("Failed to add tools.jar to classpath", e);
        }
    }
    
    private static class JvmPropsLoader extends CacheLoader<VirtualMachineDescriptor, Properties> {
        @Override
        public Properties load(VirtualMachineDescriptor key) throws Exception {
            VirtualMachine vm = null;
        
            try {
                vm = VirtualMachine.attach(key);
                return vm.getSystemProperties();
            } catch (Exception e) {
                log.error("Failed to retrieve JVM properties of " + key, e);
                return null;
            } finally {
                if (vm != null) {
                    try {
                        vm.detach();
                    } catch (IOException e) {
                        log.warn("Failed to detach from vm " + key, e);
                    }
                }
            }
        }
    }

    public static List<VirtualMachineDescriptor> listVms() {
        return VirtualMachine.list();
    }
    
    public static Properties getProps(VirtualMachineDescriptor desc) {
        try {
            return vmPropsCache.get(desc);
        } catch (ExecutionException e) {
            log.error("Failed to retrieve JVM properties of " + desc, e);
            return null;
        }
    }
}
