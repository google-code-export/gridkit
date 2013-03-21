package org.gridkit.coherence.chtest;

import java.io.Serializable;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.run.xml.XmlElement;

@SuppressWarnings("serial")
class CacheConfigInjecter implements Runnable, Serializable {

	private final XmlConfigFragment fragment;
	
	public CacheConfigInjecter(XmlConfigFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void run() {
		ConfigurableCacheFactory factory = CacheFactory.getConfigurableCacheFactory();
		XmlElement element = factory.getConfig();
		fragment.inject(element);
		factory.setConfig(element);
	}
}
