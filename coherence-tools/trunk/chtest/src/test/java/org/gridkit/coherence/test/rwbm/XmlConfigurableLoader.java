package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.vicluster.ViNode;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
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
	public Map loadAll(Collection paramCollection) {
		return null;
	}
	
	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void selfTest() {
		
		final String cacheName = "xml-configurable-A";
			
		cloud.all().fastLocalClusterPreset();
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

