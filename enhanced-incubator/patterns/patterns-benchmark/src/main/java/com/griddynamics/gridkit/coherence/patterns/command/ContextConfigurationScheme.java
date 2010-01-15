package com.griddynamics.gridkit.coherence.patterns.command;

import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;

public class ContextConfigurationScheme {

	private ManagementStrategy strategy = ManagementStrategy.COLOCATED;
	
	private String schemeName;
	
	private String contextCacheName = "enhanced-command-pattern.commands";
	private String commandCacheName = "enhanced-command-pattern.commands";
	
	private String threadPoolName;
	private String threadPoolSize;
	
	private boolean allowCrossThreadBundling;

	public String getCommandCacheName() {
		return commandCacheName;
	}

	
	
	
}
