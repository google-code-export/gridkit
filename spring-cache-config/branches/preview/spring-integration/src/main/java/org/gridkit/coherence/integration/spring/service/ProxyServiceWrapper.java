package org.gridkit.coherence.integration.spring.service;

import com.tangosol.io.Serializer;
import com.tangosol.net.Cluster;
import com.tangosol.net.MemberListener;
import com.tangosol.net.ProxyService;
import com.tangosol.net.ServiceInfo;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ServiceListener;

class ProxyServiceWrapper implements ProxyService {

	private final ProxyService service;
	private final ServicePostProcessor postProcessor;

	public ProxyServiceWrapper(ProxyService service, ServicePostProcessor postProcessor) {
		this.service = service;
		this.postProcessor = postProcessor;
	}
	
	public void configure(XmlElement config) {
		service.configure(config);
		if (postProcessor != null) {
			postProcessor.postConfigure(service);
		}
	}

	// delegates
	
	public void addMemberListener(MemberListener paramMemberListener) {
		service.addMemberListener(paramMemberListener);
	}

	public void addServiceListener(ServiceListener paramServiceListener) {
		service.addServiceListener(paramServiceListener);
	}

	public Cluster getCluster() {
		return service.getCluster();
	}

	public ClassLoader getContextClassLoader() {
		return service.getContextClassLoader();
	}

	public ServiceInfo getInfo() {
		return service.getInfo();
	}

	public Serializer getSerializer() {
		return service.getSerializer();
	}

	public Object getUserContext() {
		return service.getUserContext();
	}

	public boolean isRunning() {
		return service.isRunning();
	}

	public void removeMemberListener(MemberListener paramMemberListener) {
		service.removeMemberListener(paramMemberListener);
	}

	public void removeServiceListener(ServiceListener paramServiceListener) {
		service.removeServiceListener(paramServiceListener);
	}

	public void setContextClassLoader(ClassLoader paramClassLoader) {
		service.setContextClassLoader(paramClassLoader);
	}

	public void setUserContext(Object paramObject) {
		service.setUserContext(paramObject);
	}

	public void shutdown() {
		service.shutdown();
	}

	public void start() {
		service.start();
	}

	public void stop() {
		service.stop();
	}
}
