package org.gridkit.nimble.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JvmOps {
    private static final Logger log = LoggerFactory.getLogger(JvmOps.class);
    
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
    
    public static Map<VirtualMachineDescriptor, VirtualMachine> listVms() {
        Map<VirtualMachineDescriptor, VirtualMachine> result = new HashMap<VirtualMachineDescriptor, VirtualMachine>();
        
        for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
            try {
                result.put(desc, VirtualMachine.attach(desc));
            } catch (Exception e) {
                log.warn("Failed to attach to vm " + desc, e);
            }
        }
        
        return result;
    }
}
