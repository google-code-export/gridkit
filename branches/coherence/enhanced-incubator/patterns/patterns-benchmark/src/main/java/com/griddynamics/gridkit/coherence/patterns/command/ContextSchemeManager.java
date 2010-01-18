package com.griddynamics.gridkit.coherence.patterns.command;

import com.griddynamics.gridkit.coherence.patterns.command.ContextConfigurationScheme.DefaultContextConfigurationScheme;
import com.oracle.coherence.patterns.command.ContextConfiguration;

public interface ContextSchemeManager {

	public DefaultContextConfigurationScheme newSchemeObject();
	
	public ContextConfigurationScheme registerScheme(String name, ContextConfigurationScheme scheme);
	
	public ContextConfiguration createContextConfiguration(String schemeName);
	
}
