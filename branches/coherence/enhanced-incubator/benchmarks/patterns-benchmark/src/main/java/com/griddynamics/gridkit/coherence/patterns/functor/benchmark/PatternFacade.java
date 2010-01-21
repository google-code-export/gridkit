package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.Names;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
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
	
	public static class DefaultFacade implements PatternFacade
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
			return contextsManager.registerContext(name, ctx);
		}

		@Override
		public Future<CommandExecutionMark> submitFunctor(Identifier context, BenchmarkFunctor functor)
		{
			return functorSubmitter.submitFunctor(context, functor);
		}
	}
}
