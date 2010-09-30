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

import java.io.IOException;

import junit.framework.Assert;

import org.gridkit.coherence.integration.spring.service.DistributedCacheServiceConfiguration;
import org.junit.Test;

import com.tangosol.io.Serializer;
import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.WriteBuffer.BufferOutput;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.Service;
import com.tangosol.net.partition.KeyAssociator;
import com.tangosol.run.xml.XmlElement;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DistributedCacheServiceConfigurationTest {
	
	int serviceNo = 1;
	
	private DistributedCacheService configure(DistributedCacheServiceConfiguration conf) {
		XmlElement xconf = conf.getXmlConfiguration();

		Cluster cluster = CacheFactory.ensureCluster();
		final Service service = cluster.ensureService("test-service-" + (serviceNo++), conf.getServiceType().toString());
		service.configure(xconf);
		conf.postConfigure(service);

		return (DistributedCacheService) service;
	}
	
//	@Test
//	public void setPartitionListener() throws InterruptedException {
//		final CountDownLatch latch = new CountDownLatch(1);
//		DistributedCacheServiceConfiguration conf = new DistributedCacheServiceConfiguration();
//		PartitionListener listener = new PartitionListener() {
//			@Override
//			public void onPartitionEvent(PartitionEvent paramPartitionEvent) {
//				latch.countDown();
//			}
//		};
//		
//		conf.setPartitionListener(listener);
//		DistributedCacheService service = configure(conf);
//		service.start();
//		
//		latch.await(5, TimeUnit.SECONDS);
//		Assert.assertEquals(0, latch.getCount());
//		
//		CacheFactory.getCluster().shutdown();
//	}

	@Test
	public void setKeyAssociator() {
		DistributedCacheServiceConfiguration conf = new DistributedCacheServiceConfiguration();
		KeyAssociator bean = new KeyAssociator() {
			@Override
			public void init(PartitionedService paramPartitionedService) {
			}

			@Override
			public Object getAssociatedKey(Object paramObject) {
				return null;
			}
		};
				
		conf.setKeyAssociator(bean);
		DistributedCacheService service = configure(conf);
		service.start();
		
		Assert.assertSame(bean, service.getKeyAssociator());
		
		CacheFactory.getCluster().shutdown();
	}

	@Test
	public void setSerializer() {
		DistributedCacheServiceConfiguration conf = new DistributedCacheServiceConfiguration();
		Serializer bean = new Serializer() {
			@Override
			public Object deserialize(BufferInput paramBufferInput) throws IOException {
				return null;
			}

			@Override
			public void serialize(BufferOutput paramBufferOutput, Object paramObject) throws IOException {
			}
		};
		
		conf.setSerializer(bean);
		DistributedCacheService service = configure(conf);
		service.start();
		
		Assert.assertSame(bean, service.getSerializer());
		
		CacheFactory.getCluster().shutdown();
	}

}
