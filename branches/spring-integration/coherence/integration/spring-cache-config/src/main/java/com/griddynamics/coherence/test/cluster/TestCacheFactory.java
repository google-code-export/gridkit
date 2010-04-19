package com.griddynamics.coherence.test.cluster;

import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

public class TestCacheFactory extends DefaultConfigurableCacheFactory {
	
	public TestCacheFactory(String config) {
		super(loadConfig(config));
	}

	@Override
	public XmlElement getConfig() {
		System.out.println("getConfig");
		return super.getConfig();
	}
	
	@Override
	public void setConfig(XmlElement xmlConfig) {
		System.out.println("setConfig");
		super.setConfig(xmlConfig);
	}
	
	@Override
	public NamedCache ensureCache(String sCacheName, ClassLoader loader) {
		System.out.println("ensureCache: "+sCacheName);
		return super.ensureCache(sCacheName, loader);
	}
	
	@Override
	public Service ensureService(String sServiceName) {
		System.out.println("ensureService: "+sServiceName);
		return super.ensureService(sServiceName);
	}
	
	@Override
	public void releaseCache(NamedCache cache) {
		System.out.println("releaseCache");
		super.releaseCache(cache);
	}
	
	@Override
	public void destroyCache(NamedCache cache) {
		System.out.println("destroyCache");
		super.destroyCache(cache);
	}
}
