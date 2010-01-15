package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.InvocationService;

public interface PatternFacade
{
	public Future<CommandExecutionMark> submitFunctor(Identifier context, BenchmarkFunctor functor);
	public Identifier registerContext(String name, SimpleContext ctx);
	public InvocationService getInvocationService();
	//InvocationService invocationService = (InvocationService)CacheFactory.getService(Names.invocationService);
}
