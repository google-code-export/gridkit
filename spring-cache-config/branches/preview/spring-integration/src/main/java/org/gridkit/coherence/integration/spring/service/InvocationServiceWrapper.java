package org.gridkit.coherence.integration.spring.service;

import java.util.Map;
import java.util.Set;

import com.tangosol.io.Serializer;
import com.tangosol.net.Cluster;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.MemberListener;
import com.tangosol.net.ServiceInfo;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ServiceListener;

class InvocationServiceWrapper implements InvocationService {

	private final InvocationService service;
	private final ServicePostProcessor postProcessor;

	public InvocationServiceWrapper(InvocationService service, ServicePostProcessor postProcessor) {
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

	@SuppressWarnings("unchecked")
	public void execute(Invocable paramInvocable, Set paramSet,
			InvocationObserver paramInvocationObserver) {
		service.execute(paramInvocable, paramSet, paramInvocationObserver);
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

	@SuppressWarnings("unchecked")
	public Map query(Invocable paramInvocable, Set paramSet) {
		return service.query(paramInvocable, paramSet);
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
