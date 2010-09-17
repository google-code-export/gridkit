/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

import org.gridkit.coherence.integration.spring.impl.ClusteredServiceBean;
import org.gridkit.coherence.utils.classloader.Isolate;
import org.gridkit.coherence.utils.classloader.IsolateTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.RequestPolicyException;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
// Need to run in isolated class loader, otherwise Coherence defaults are not going to be overridden by system properties
@Ignore
@RunWith(IsolateTestRunner.class)
public class RemoteStorageSchemeContextTest extends BaseSimpleContextTest {
	
	static Isolate node1;
	
	@BeforeClass
	public static void init() {

		node1 = new Isolate("node1", "com.tangosol", "org.gridkit");
		node1.start();
		node1.submit(StartCmd.class.getName());

		// TODO default property reset for tests
		System.setProperty("tangosol.coherence.wka", "localhost");
		System.setProperty("tangosol.coherence.distributed.localstorage", "false");
		context = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
//		clientContext = new ClassPathXmlApplicationContext("schema/extend-client-context.xml");		
	}
	
	@AfterClass
	public static void shutdown() {
		context = null;
//		clientContext = null;
		CacheFactory.getCluster().shutdown();
		node1.submit(StopCmd.class.getName());
		node1.stop();
	}
	
	@Test(expected = RequestPolicyException.class)
	public void testCacheE_Serializer() {
		super.testCacheE_Serializer();
	}
	
	public static class StartCmd implements Runnable {
		@Override
		public void run() {
			System.setProperty("tangosol.coherence.wka", "localhost");
			System.setProperty("tangosol.coherence.distributed.localstorage", "true");
			ApplicationContext appCtx = new ClassPathXmlApplicationContext("schema/simple-coherence-context.xml");
			((ClusteredServiceBean)appCtx.getBean("default.distributed.service")).getCoherenceService();
		}
	}
	
	public static class StopCmd implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCluster().shutdown();
		}
	}

}
