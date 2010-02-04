/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.griddynamics.gridkit.coherence.patterns.command.benchmark.incubator;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Names;
import com.griddynamics.gridkit.coherence.patterns.command.benchmark.PatternFacade;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

public class IncubatorPatternFacade implements PatternFacade {

	private ContextsManager ctxMan = DefaultContextsManager.getInstance();
	private CommandSubmitter submitter = DefaultCommandSubmitter.getInstance();
	private ManagementStrategy strategy;

	public IncubatorPatternFacade() {
		setSysProp("benchmark.command-pattern.storeStrategy", ManagementStrategy.COLOCATED.name());
		String mode = System.getProperty("benchmark.command-pattern.storeStrategy");
	    strategy = ManagementStrategy.valueOf(mode);
	}
	
	@Override
	public Identifier registerContext(String name, Context ctx) {
		ContextConfiguration conf = new DefaultContextConfiguration(strategy);
		return ctxMan.registerContext(name, ctx, conf);
	}

	@Override
	public <T extends Context> Identifier submit(Identifier id, Command<T> command) {
		return submitter.submitCommand(id, command);
	}

	@Override
	public InvocationService getInvocationService()
	{
		return (InvocationService)CacheFactory.getService(Names.invocationService);
	}
	
	

}
