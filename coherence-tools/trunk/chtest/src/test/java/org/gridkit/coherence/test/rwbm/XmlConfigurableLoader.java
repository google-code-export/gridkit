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
package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.run.xml.XmlConfigurable;
import com.tangosol.run.xml.XmlElement;

public class XmlConfigurableLoader implements CacheLoader, XmlConfigurable {

	XmlElement config = null;
	
	String paramA;
	
	public XmlConfigurableLoader() {
		System.out.println("Created");
	}
	
	@Override
	public XmlElement getConfig() {
		return config;
	}

	@Override
	public void setConfig(XmlElement config) {
		this.config = config;
		// notice, xml is transformed !!!
		// param names are transformed to element names
		paramA = config.getSafeElement("paramA").getString();
	}

	@Override
	public Object load(Object paramObject) {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map loadAll(Collection paramCollection) {
		return null;
	}
	
	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void selfTest() {
		
		final String cacheName = "xml-configurable-A";
			
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/cache-store-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		
		storage.getCache(cacheName);
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		storage.autoStartServices().touch();
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);

				cache.put("A", "A");
				
				return null;
			}
		});
	}
}

