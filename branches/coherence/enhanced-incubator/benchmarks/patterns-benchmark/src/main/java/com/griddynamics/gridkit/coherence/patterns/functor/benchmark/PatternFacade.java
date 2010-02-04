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
package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Names;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.functor.DefaultFunctorSubmitter;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

public interface PatternFacade
{
	public Future<CommandExecutionMark> submitFunctor(Identifier context, BenchmarkFunctor functor);
	public Identifier registerContext(String name, SimpleContext ctx);
	public InvocationService getInvocationService();
	
	public static class DefaultFacade extends com.griddynamics.gridkit.coherence.patterns.benchmark.PatternFacade implements PatternFacade
	{
		static private final DefaultFacade instance = new DefaultFacade();
		
		public static DefaultFacade getInstance()
		{
			return instance;
		}

		private final  ContextsManager contextsManager;
		private final FunctorSubmitter functorSubmitter;
		
		private DefaultFacade()
		{
			super();
			contextsManager  = DefaultContextsManager.getInstance();
			functorSubmitter = DefaultFunctorSubmitter.getInstance();
		}
		
		@Override
		public InvocationService getInvocationService()
		{
			return (InvocationService)CacheFactory.getService(Names.invocationService);
		}

		@Override
		public Identifier registerContext(String name, SimpleContext ctx)
		{
			return contextsManager.registerContext(name, ctx, conf);
		}

		@Override
		public Future<CommandExecutionMark> submitFunctor(Identifier context, BenchmarkFunctor functor)
		{
			return functorSubmitter.submitFunctor(context, functor);
		}
	}
}
