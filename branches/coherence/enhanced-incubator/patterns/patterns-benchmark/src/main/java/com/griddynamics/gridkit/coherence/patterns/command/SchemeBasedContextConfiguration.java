package com.griddynamics.gridkit.coherence.patterns.command;

import com.oracle.coherence.patterns.command.ContextConfiguration;

class SchemeBasedContextConfiguration implements ContextConfiguration {

	private ContextConfigurationScheme scheme;

	public SchemeBasedContextConfiguration(ContextConfigurationScheme scheme) {
		this.scheme = scheme;
	}
	
	@Override
	public ManagementStrategy getManagementStrategy() {
		return scheme.getCommandsPlacementStrategy();
	}
	
	public ContextConfigurationScheme getScheme() {
		return scheme;
	}
}
