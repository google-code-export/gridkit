package org.gridkit.lab.tentacle;

import org.gridkit.lab.tentacle.JmxHost.JmxHostSource;

public class AttachJmxTarget {

	public static JmxProcessExpander X = new JmxProcessExpander();
	
	
	public static class JmxProcessExpander implements Locator<LocalJvmProcess, JmxHost, JmxHostSource> {
		
	}
}
