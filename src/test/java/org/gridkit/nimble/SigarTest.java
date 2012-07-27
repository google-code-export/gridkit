package org.gridkit.nimble;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarProxy;
import org.junit.Ignore;

@Ignore
public class SigarTest {
    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().load("C:/Tools/hyperic-sigar-1.6.4/sigar-bin/lib/sigar-x86-winnt.dll");
        
        SigarProxy sigar = new Sigar();
        
        System.out.println(sigar.getNetStat().getTcpEstablished());
    }
}
