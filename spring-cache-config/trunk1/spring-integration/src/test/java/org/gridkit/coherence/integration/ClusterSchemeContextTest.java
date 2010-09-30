/**
- * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.integration;

import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.gridkit.coherence.utils.classloader.Isolate;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ClusterSchemeContextTest extends BaseSimpleContextTest {

	public static Isolate node1;
	public static Isolate node2;
	
	@BeforeClass
	public static void init() {
		System.setProperty("tangosol.coherence.wka", "localhost");
		node1 = new Isolate("node1", "com.tangosol", "org.gridkit");
		node1.start();
		node1.submit(StartCmd.class.getName());
		node2 = new Isolate("node2", "com.tangosol", "org.gridkit");
		node2.start();
		node2.submit(StartCmd.class.getName());
		context = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
	}
	
	@AfterClass
	public static void shutdown() {
		context = null;
		CacheFactory.getCluster().shutdown();
		node1.submit(StopCmd.class.getName());
		node1.stop();
		node1 = null;
		node2.submit(StopCmd.class.getName());
		node2.stop();
		node2 = null;
	}
	
	@Test
	public void testCluster() {
		CacheFactory.ensureCluster();
		Set<?> members = CacheFactory.getCluster().getMemberSet();
		Assert.assertEquals(6, members.size());
	}
	
	@Ignore
	public void testCacheF_Evictor() {
		// cannot test in multiple nodes 
	}
	
	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");
		ApplicationContext appCtx = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
		appCtx.getBean("default.distributed.service");
		while(true) {
			LockSupport.parkNanos(1<<30);
		}
	}
	
	public static class StartCmd implements Runnable {
		@Override
		public void run() {
			ApplicationContext appCtx = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
			appCtx.getBean("cache.A");
		}
	}
	
	public static class StopCmd implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCluster().shutdown();
		}
	}
	
}
