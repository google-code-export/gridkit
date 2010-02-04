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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Names;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

public interface PatternFacade
{
	public Identifier registerContext(String name, Context ctx);
	
	public <T extends Context> Identifier submit(Identifier id, Command<T> command);
	
	public InvocationService getInvocationService();
	
	public static class DefaultFacade extends com.griddynamics.gridkit.coherence.patterns.benchmark.PatternFacade implements PatternFacade
	{
		static private final PatternFacade instance;
		
		static
		{
			//TODO logic of PatternFacade.Helper class should be here
			instance = new DefaultFacade();
		}
		
		public static PatternFacade getInstance()
		{
			return instance;
		}
		
		private final ContextsManager  ctxMan;
		private final CommandSubmitter submitter;
		
		private DefaultFacade()
		{
			super();
			ctxMan    = DefaultContextsManager.getInstance();
			submitter = DefaultCommandSubmitter.getInstance();
		}

		@Override
		public InvocationService getInvocationService()
		{
			return (InvocationService)CacheFactory.getService(Names.invocationService);
		}

		@Override
		public Identifier registerContext(String name, Context ctx)
		{
			return ctxMan.registerContext(name, ctx, conf);
		}

		@Override
		public <T extends Context> Identifier submit(Identifier id, Command<T> command)
		{
			return submitter.submitCommand(id, command);
		}
	}
	
	/*
	public static class Helper {		
		public static PatternFacade create() {
			//TestHelper.setSysProp("benchmark.facadeClass", DumbInvokationCommandFacade.class.getName());
			setSysProp("benchmark.facadeClass", IncubatorPatternFacade.class.getName());
			String name = System.getProperty("benchmark.facadeClass");
			System.out.println("Pattern implementation: " + name);
			try {
				PatternFacade facade = (PatternFacade) Class.forName(name).newInstance();
				return facade;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
				throw new RuntimeException();
			}
		}
	}
	*/
}
