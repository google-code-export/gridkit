package org.gridkit.util.coherence.extendconn;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class ExtendConnection {

	private static Logger LOGGER = Logger.getLogger(ExtendConnection.class.getName());
	
	private static AtomicInteger CONNECTION_COUNTER = new AtomicInteger();

	private int connectionId = CONNECTION_COUNTER.incrementAndGet();
	private ConfigurableCacheFactory cacheFactory;
	private ConcurrentMap<Service, Service> activeServices = new ConcurrentHashMap<Service, Service>(4, 0.5f, 2);
	
	public ExtendConnection(String configFile) {
		cacheFactory = initPrivateCacheFactory(configFile);
	}

	private DefaultConfigurableCacheFactory initPrivateCacheFactory(String configFile) {
		LOGGER.info("New Extend connection #" + connectionId + " is going to be created, config: " + configFile);

		XmlElement xml = XmlHelper.loadFileOrResource(configFile, "Coherence cache configuration for Extend connection #" + connectionId);
		// transforming configuration
		XmlElement schemes = xml.getSafeElement("caching-schemes");
		for(Object o: schemes.getElementList()) {
			XmlElement scheme = (XmlElement) o;
			if (isRemoteScheme(scheme)) {
				String name = scheme.getSafeElement("service-name").getString();
				if (name != null) {
					String nname = name + "-" + connectionId;
					scheme.getElement("service-name").setString(nname);
				}
			}
		}
		
		DefaultConfigurableCacheFactory factory = new DefaultConfigurableCacheFactory(xml);
		return factory;
	}
	
    
    private boolean isRemoteScheme(XmlElement scheme) {
		String name = scheme.getName();
		return "remote-cache-scheme".equals(name) || "remote-invocation-scheme".equals(name);
	}


    public NamedCache getCache(String name) {
        NamedCache cache = cacheFactory.ensureCache(name, null);
        Service service = cache.getCacheService();
        activeServices.putIfAbsent(service, service);
        return cache;
    }

	public InvocationService getInvocationService(String serviceName) {
		InvocationService service = (InvocationService) cacheFactory.ensureService(serviceName + "-" + connectionId);
		activeServices.putIfAbsent(service, service);
		return service;
	}

	/**
	 * Warning: this method is not concurrency safe, you may get to trouble if you are accessing caches of services via this connection during shutdown.
	 */
	public void disconnect() {
		for(Service service:  new ArrayList<Service>(activeServices.keySet())) {
			try {
				if (service.isRunning()) {
					service.stop();
				}
			}
			catch(Exception e) {
				LOGGER.log(Level.WARNING, "Exception during remote service shutdown", e);
			}
		}
	}
}
