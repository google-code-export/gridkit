package org.gridkit.coherence.chtest;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.vicluster.isolate.Isolate;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

@Ignore
public class PartialEPCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void testPartialEPFailure() throws InterruptedException {
		
		CohNode all = cloud.node("**");
		
		all.setProp("tangosol.coherence.guard.timeout", "5s");
		all.setProp("tangosol.coherence.distributed.task.hung", "5s");
		all.setProp("tangosol.coherence.distributed.task.timeout", "5s");
//		all.setProp("tangosol.coherence.distributed.threads", "4");
//		all.setProp("tangosol.coherence.distributed.request.timeout", "10s");
		
		all.fastLocalClusterPreset();		
		all.enableJmx(true);

		cloud.node("server*")
			.localStorage(true)
			.autoStartServices();
		
		for(int i = 1; i <= 3; ++i) {
			cloud.nodes("server" + i);
		}
				
		CohNode client = cloud.node("client");
		
		client.localStorage(false);
		
		all.getCache("distr-A");
		
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
