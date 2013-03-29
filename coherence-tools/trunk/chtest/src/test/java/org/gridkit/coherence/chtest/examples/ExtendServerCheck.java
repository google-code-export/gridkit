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
package org.gridkit.coherence.chtest.examples;

import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.NamedCache;

public class ExtendServerCheck {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Test
	public void start_server() throws InterruptedException {
		
		cloud.all()
//			.pofConfig("extend-test-pof-config.xml")
			.pofEnabled(true);

		cloud.node("cluster.**")
			.cacheConfig("extend-server-cache-config.xml");		
			
		cloud.node("xclient.**")
			.enableTCMP(false)
			.cacheConfig("extend-client-cache-config.xml");

		
		cloud.node("cluster.storage.**")
			.autoStartServices()
			.localStorage(true);

		cloud.node("cluster.proxy.**")
			.autoStartServices()
			.localStorage(false);
	
		cloud.node("cluster.storage.1");
		cloud.node("cluster.proxy.1");
		
		cloud.all().getCache("cache");
		
		cloud.node("cluster.proxy.1").ensureService("ExtendTcpProxyService");
		
		NamedCache cache = cloud.node("cluster.proxy.1").getCache("cache");
		
		for(int i = 0; i != 1000; ++i) {
			cache.put("Key-" + i, i);
		}
		
		cloud.node("xclient.1").getCache("cache");
		
		System.out.println("Cache started");
		
		Thread.sleep(TimeUnit.HOURS.toMillis(2));
	}
}
