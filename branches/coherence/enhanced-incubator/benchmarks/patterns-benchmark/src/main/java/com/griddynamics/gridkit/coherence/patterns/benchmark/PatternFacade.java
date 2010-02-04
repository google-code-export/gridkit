package com.griddynamics.gridkit.coherence.patterns.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;

import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;

public class PatternFacade
{
	protected final ManagementStrategy   strategy;
	protected final ContextConfiguration conf;
	
	protected PatternFacade()
	{
		setSysProp("benchmark.command-pattern.storeStrategy", ManagementStrategy.COLOCATED.name());
		String mode = System.getProperty("benchmark.command-pattern.storeStrategy");
	    strategy = ManagementStrategy.valueOf(mode);
	    
	    conf = new DefaultContextConfiguration(strategy);
	}
}
