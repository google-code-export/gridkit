/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class JvmId {

    public static final String JVM_ID; 
    static {
        String id = ManagementFactory.getRuntimeMXBean().getName();
        if (id.indexOf('.') > 0) {
            id = id.substring(0, id.indexOf('.'));
        }
        JVM_ID = id.replace('@', '-');
    }
    public static final String LOCALHOST;
    static {
        try {
            LOCALHOST = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot get localhost name");
        }
    }

    public static final int PROCESS_ID;
    static {
        int p = JVM_ID.indexOf("@");
        if (p > 0) {
            String pid = JVM_ID.substring(0, p);
            PROCESS_ID = Integer.parseInt(pid);
        }
        else {
            PROCESS_ID = -1;
        }        
    }
    
    public static void main(String[] args) {
        System.out.println(JVM_ID);
    }
}
