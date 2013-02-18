package org.gridkit.util.coherence.cohtester;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.util.coherence.cohtester.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

//@Ignore
public class PartialEPCheck {

	@Test
	public void testPartialEPFailure() throws InterruptedException {
		
		ViCluster cluster = new ViCluster("testPartialEPFailure", "com.tangosol", "org.gridkit");
		
		cluster.setProp("tangosol.coherence.guard.timeout", "5s");
		cluster.setProp("tangosol.coherence.distributed.task.hung", "5s");
		cluster.setProp("tangosol.coherence.distributed.task.timeout", "5s");
//		cluster.setProp("tangosol.coherence.distributed.threads", "4");
//		cluster.setProp("tangosol.coherence.distributed.request.timeout", "10s");
		
		CohHelper.enableFastLocalCluster(cluster);
		CohHelper.enableJmx(cluster);

		cluster.node("first-node").getCluster();
		
		ViNode server1 = cluster.node("server1"); 
		ViNode server2 = cluster.node("server2"); 
		ViNode server3 = cluster.node("server3"); 
		
		server1.start(DefaultCacheServer.class);
		server2.start(DefaultCacheServer.class);
		server3.start(DefaultCacheServer.class);
		
		ViNode client = cluster.node("client");
		
		CohHelper.localstorage(client, false);

		server1.getCache("distr-A");
		server2.getCache("distr-A");
		server3.getCache("distr-A");
		
		NamedCache cache =  client.getCache("distr-A");
		for (int i = 0; i != 1000; ++i) {
			cache.put(i, "x" + i);
		}
		
		Thread.sleep(1000);
		
		client.exec(new Runnable() {			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {
				
				NamedCache cache =  CacheFactory.getCache("distr-A");
				
				HashSet keySet = new HashSet(cache.keySet());
				
				Map result = cache.invokeAll(keySet, new TestCrashEP());
				
				Set values = new HashSet(result.values());
				
				Assert.assertEquals("Result set size", keySet.size(), values.size());
			}
		});
		
		cluster.kill();
	}
	
	@SuppressWarnings("serial")
	public static class TestFailureEP extends AbstractProcessor implements Serializable {
		
		@SuppressWarnings("rawtypes")
		@Override
		public Map processAll(Set setEntries) {
			System.out.println("Processing set " + setEntries.size());
			return super.processAll(setEntries);
		}

		@Override
		public Object process(Entry entry) {
			Integer key = (Integer) entry.getKey();
			if (key == 21) {
				try {
					Thread.sleep(600000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			
			return entry.isPresent() ? key : null;
		}		
	}

	@SuppressWarnings("serial")
	public static class TestCrashEP extends AbstractProcessor implements Serializable {
		
		@SuppressWarnings("rawtypes")
		@Override
		public Map processAll(Set setEntries) {
//			System.out.println("Processing set " + setEntries.size());
			return super.processAll(setEntries);
		}
		
		@Override
		@SuppressWarnings("deprecation")
		public Object process(Entry entry) {
			Integer key = (Integer) entry.getKey();
			
			if (key == 21 && entry.isPresent()) {
				System.out.println("Thread: " + Thread.currentThread().getName());
				ThreadGroup tg = Thread.currentThread().getThreadGroup();
				while(!tg.getClass().getName().startsWith(Isolate.class.getName())) {
					tg = tg.getParent();
				}
				System.out.println("Brutally killing context node, " + tg);
				tg.suspend();
			}
			
			return entry.isPresent() ? key : null;
		}		
	}
	
}
