package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;

public class SingleRunFunctorBenchmark
{
	public static void main(String[] args)
	{
		// Configure coherence
		setSysProp("tangosol.pof.config"                        , "benchmark-pof-config.xml");
		setSysProp("tangosol.coherence.cacheconfig"             , "benchmark-pof-cache-config.xml");
		setSysProp("tangosol.coherence.clusterport"             , "9001");
		setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		setSysProp("benchmark.functor.threadCount",  "10");
		setSysProp("benchmark.functor.contextCount", "100");
		setSysProp("benchmark.functor.memberCount",  "2");
		
		setSysProp("benchmark.functor.functor"            , "touch");
		setSysProp("benchmark.functor.opsPerSec"          , "0");
        setSysProp("benchmark.functor.invocationPerThread", "1000");
        
        int threadCount = Integer.getInteger("benchmark.functor.threadCount");
        int contextCount = Integer.getInteger("benchmark.functor.contextCount");
        int memberCount = Integer.getInteger("benchmark.functor.memberCount");
        
        String functor = System.getProperty("benchmark.functor.functor");
        int opsPerSec = Integer.getInteger("benchmark.functor.opsPerSec");
        int invocationPerThread = Integer.getInteger("benchmark.functor.invocationPerThread");
        
        PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
        @SuppressWarnings("unchecked")
		Set<Member> members = facade.getInvocationService().getInfo().getServiceMembers();
		
        members.remove(CacheFactory.getCluster().getLocalMember());
		
		if (members.size() < memberCount)
		{
			throw new RuntimeException("Not enought cluster members to start SingleRunFunctorBenchmark");
		}
		
		int threadCountStep = threadCount / memberCount;
		int threadCountAdd  = threadCount % memberCount;
		
		Map<Member,FunctorBenchmarkWorkerParams> workers = new HashMap<Member,FunctorBenchmarkWorkerParams>();
		
		for (Member m : members)
		{
			int memberThreadCount = threadCountStep;
			if (threadCountAdd > 0)
			{
				threadCountAdd--;
				memberThreadCount++;
			}
			
			workers.put(m, new FunctorBenchmarkWorkerParams(functor, memberThreadCount, invocationPerThread, opsPerSec));
		}
		
		FunctorBenchmarkDispatcher dispatcher = new FunctorBenchmarkDispatcher(contextCount);
		
		FunctorBenchmarkStats res = dispatcher.execute(facade, workers);
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println(res.toString());
		System.out.print("Java MS stats");
		System.out.println(res.getJavaMsStats().toString());
		System.out.print("Java NS stats");
		System.out.println(res.getJavaNsStats().toString());
		System.out.print("Coherence MS stats");
		System.out.println(res.getCoherenceMsStats().toString());
		System.out.println("--------------------------------------------------------------------------------");
	}
	
	private static void setSysProp(String prop, String value)
	{
		if (System.getProperty(prop) == null)
		{
			System.setProperty(prop, value);
		}
		System.out.println("[SysProp] " + prop + ": " + System.getProperty(prop));
	}
}
