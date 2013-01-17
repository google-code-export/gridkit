package org.gridkit.coherence.test.rwbm;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
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
	
	@Test
	public void selfTest() {
		
		final String cacheName = "xml-configurable-A";
		
		ViCluster cluster = new ViCluster("selfTest", "org.gridkit", "com.tangosol");
		try {
			
			CohHelper.enableFastLocalCluster(cluster);
			CohHelper.cacheConfig(cluster, "/cache-store-cache-config.xml");
			
			ViNode storage = cluster.node("storage");
			CohHelper.localstorage(storage, true);
			
			storage.getCache(cacheName);
			
			ViNode client = cluster.node("client");
			CohHelper.localstorage(client, false);
			
			storage.start(DefaultCacheServer.class);
			
			client.exec(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					
					NamedCache cache = CacheFactory.getCache(cacheName);

					cache.put("A", "A");
					
					return null;
				}
			});
		}
		finally {
			cluster.shutdown();
		}		
	}
}

