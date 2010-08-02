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

package org.gridkit.coherence.integration.spring.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;
import com.tangosol.util.ServiceEvent;
import com.tangosol.util.ServiceListener;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ServiceListenersTest {

	static ApplicationContext context;
	
	@BeforeClass
	public static void init() {

		System.setProperty("tangosol.coherence.wka", "localhost");
		context = new ClassPathXmlApplicationContext("config/service-listener-coherence-context.xml");
		
	}
	
	@AfterClass
	public static void shutdown() {
		context = null;
		CacheFactory.getCluster().shutdown();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testCacheService() {
		Map<String, String> log = (Map<String, String>) context.getBean("log-map");
		DistributedCacheService dcs = (DistributedCacheService) context.getBean("cache-service");
			
		Assert.assertTrue(log.containsKey("member-listener1-joined"));
		Assert.assertTrue(log.containsKey("service-listener1-starting"));
		Assert.assertTrue(log.containsKey("service-listener1-started"));
		
		dcs.shutdown();
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));

		Assert.assertTrue(log.containsKey("member-listener1-leaving"));
		Assert.assertTrue(log.containsKey("member-listener1-left"));
		Assert.assertTrue(log.containsKey("service-listener1-stopping"));
		Assert.assertTrue(log.containsKey("service-listener1-stopped"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testExecService() {
		Map<String, String> log = (Map<String, String>) context.getBean("log-map");
		InvocationService is = (InvocationService) context.getBean("exec-service");
		
		Assert.assertTrue(log.containsKey("member-listener2-joined"));
		Assert.assertTrue(log.containsKey("service-listener2-starting"));
		Assert.assertTrue(log.containsKey("service-listener2-started"));

		Assert.assertTrue(log.containsKey("member-listener3-joined"));
		Assert.assertTrue(log.containsKey("service-listener3-starting"));
		Assert.assertTrue(log.containsKey("service-listener3-started"));
		
		is.shutdown();
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
		
		Assert.assertTrue(log.containsKey("member-listener2-leaving"));
		Assert.assertTrue(log.containsKey("member-listener2-left"));
		Assert.assertTrue(log.containsKey("service-listener2-stopping"));
		Assert.assertTrue(log.containsKey("service-listener2-stopped"));

		Assert.assertTrue(log.containsKey("member-listener3-leaving"));
		Assert.assertTrue(log.containsKey("member-listener3-left"));
		Assert.assertTrue(log.containsKey("service-listener3-stopping"));
		Assert.assertTrue(log.containsKey("service-listener3-stopped"));
	}
	
	
	public static class LogMemberListener implements MemberListener, BeanNameAware {
		
		private String beanName;
		private Map<String, String> log;
		
		@Override
		public void setBeanName(String name) {
			this.beanName = name;
		}
		
		@Required
		public void setLog(Map<String, String> log) {
			this.log = log;
		}
		
		@Override
		public void memberJoined(MemberEvent event) {
			String key = beanName + "-joined";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}

		@Override
		public void memberLeaving(MemberEvent event) {
			String key = beanName + "-leaving";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}

		@Override
		public void memberLeft(MemberEvent event) {
			String key = beanName + "-left";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}
	}

	public static class LogServiceListener implements ServiceListener, BeanNameAware {
		
		private String beanName;
		private Map<String, String> log;
		
		@Override
		public void setBeanName(String name) {
			this.beanName = name;
		}
		
		@Required
		public void setLog(Map<String, String> log) {
			this.log = log;
		}
		
		@Override
		public void serviceStarting(ServiceEvent event) {
			String key = beanName + "-starting";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}
		
		@Override
		public void serviceStarted(ServiceEvent event) {
			String key = beanName + "-started";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}

		@Override
		public void serviceStopping(ServiceEvent event) {
			String key = beanName + "-stopping";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}
		
		@Override
		public void serviceStopped(ServiceEvent event) {
			String key = beanName + "-stopped";
			String value = log.get(key);
			value = value == null ? "" : value + "\n";
			value += event.toString();
			log.put(key, value);
		}
	}
}
