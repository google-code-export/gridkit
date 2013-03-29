/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.check;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.CohHelper;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.test.CacheTemplate;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.WrapperException;
import com.tangosol.util.processor.AbstractProcessor;

public class TaskTimeoutSematicCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void shutdown_cluster_after_test() {
		executor.shutdownNow();
	}

	@Test
	public void verify_task_interruption() throws InterruptedException, ExecutionException {
		
		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedServiceThreadCount(cloud.all(), 2);
		CacheTemplate.usePartitionedServiceTaskTimeout(cloud.all(), 1000);

		CohNode storage = cloud.node("storage");
		CohHelper.localstorage(storage, true);
		CohNode client = cloud.node("client");
		CohHelper.localstorage(client, false);

		storage.getCache("a-test").put("A", "A");
		
		try {
			client.exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					NamedCache cache = CacheFactory.getCache("a-test");
					cache.invoke("A", new SlowProcessor());
					return null;
				}			
			});
			Assert.assertFalse("Exception is expected", true);
		}
		catch(WrapperException e) {
			Assert.assertSame(TaskInterruptedException.class, e.getCause().getClass());
		}
	}

	@Test
	public void verify_task_interruption2() throws InterruptedException, ExecutionException {
		
		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedServiceThreadCount(cloud.all(), 2);
		CacheTemplate.usePartitionedServiceGuardianTimeout(cloud.all(), 1000);

		CohNode storage = cloud.node("storage");
		CohHelper.localstorage(storage, true);
		CohNode client = cloud.node("client");
		CohHelper.localstorage(client, false);

		storage.getCache("a-test").put("A", "A");
		
		try {
			client.exec(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					NamedCache cache = CacheFactory.getCache("a-test");
					cache.invoke("A", new SlowProcessor());
					return null;
				}			
			});
			Assert.assertFalse("Exception is expected", true);
		}
		catch(WrapperException e) {
			Assert.assertSame(TaskInterruptedException.class, e.getCause().getClass());
		}
	}

	@Test
	public void verify_task_hung_does_not_interrupt() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedServiceThreadCount(cloud.all(), 2);
		CacheTemplate.usePartitionedServiceTaskTimeout(cloud.all(), 30000);
		CacheTemplate.usePartitionedServiceTaskHungThreshold(cloud.all(), 1000);

		CohNode storage = cloud.node("storage");
		CohHelper.localstorage(storage, true);
		CohNode client = cloud.node("client");
		CohHelper.localstorage(client, false);

		storage.getCache("a-test").put("A", "A");
		
		client.exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				NamedCache cache = CacheFactory.getCache("a-test");
				cache.invoke("A", new SlowProcessor());
				return null;
			}			
		});
	}

	@Test
	public void verify_abandoning_threads() throws InterruptedException, ExecutionException {

		cloud.all().presetFastLocalCluster();
		
		CacheTemplate.useTemplateCacheConfig(cloud.all());
		CacheTemplate.usePartitionedInMemoryCache(cloud.all());
		CacheTemplate.usePartitionedServicePartitionCount(cloud.all(), 1023);
		CacheTemplate.usePartitionedServiceThreadCount(cloud.all(), 2);
		CacheTemplate.usePartitionedServiceTaskTimeout(cloud.all(), 5000);
		CacheTemplate.usePartitionedServiceTaskHungThreshold(cloud.all(), 10000);

		CohNode storage = cloud.node("storage");
		CohHelper.localstorage(storage, true);
		final CohNode client1 = cloud.node("client1");
		CohHelper.localstorage(client1, false);
		final CohNode client2 = cloud.node("client2");
		CohHelper.localstorage(client2, false);
		final CohNode client3 = cloud.node("client3");
		CohHelper.localstorage(client3, false);

		storage.getCache("a-test").put("A", "A");
		storage.getCache("a-test").put("B", "B");
		
		client1.getCache("a-test").get("A");
		client2.getCache("a-test").get("B");
		client3.getCache("a-test").get("C");

		executor.submit(new Runnable(){
			@Override
			public void run() {
				client1.exec(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						NamedCache cache = CacheFactory.getCache("a-test");
						cache.invoke("A", new HungProcessor());
						return null;
					}			
				});
			}			
		});

		executor.submit(new Runnable(){
			@Override
			public void run() {
				client2.exec(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						NamedCache cache = CacheFactory.getCache("a-test");
						cache.invoke("B", new HungProcessor());
						return null;
					}			
				});
			}			
		});
		
		Thread.sleep(500);
		
		long ts = System.nanoTime();
		client3.getCache("a-test").get("C");
		long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ts);
		System.out.println("Get done is " + time +"ms");
		
		Assert.assertTrue("Get delay should be [4500, 5000)", time >= 4500 && time < 5000);
	}
	
	public static void delay(long millis) {
		try {
			long deadLine = System.currentTimeMillis() + millis + 2;
			while(true) {
				long sleepTime = deadLine - System.currentTimeMillis();
				if (sleepTime > 0) {
					Thread.sleep(sleepTime);
				}
				else {
					break;
				}
			}
		} catch (InterruptedException e) {
			throw new TaskInterruptedException(e);
		}
	}	

	@SuppressWarnings("serial")
	public static class SlowProcessor extends AbstractProcessor implements Serializable {

		@Override
		public Object process(Entry arg0) {
			System.out.println("Slow processor: " + arg0.getKey());
			delay(10000);
			return null;
		}
	}

	@SuppressWarnings("serial")
	public static class HungProcessor extends AbstractProcessor implements Serializable {

		@Override
		public Object process(Entry arg0) {
			System.out.println("Hung processor: " + arg0.getKey());
			while(true) {
				try {
					delay(1000);
				}
				catch(RuntimeException e) {
					// ignore
				}
			}
		}
	}
	
	@SuppressWarnings("serial")
	public static class TaskInterruptedException extends RuntimeException {

		public TaskInterruptedException() {
			super();
		}

		public TaskInterruptedException(String message, Throwable cause) {
			super(message, cause);
		}

		public TaskInterruptedException(String message) {
			super(message);
		}

		public TaskInterruptedException(Throwable cause) {
			super(cause);
		}
	}
	
}
