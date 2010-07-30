package org.gridkit.coherence.integration.spring.impl;

import java.util.concurrent.Callable;

import org.gridkit.coherence.integration.spring.ClusteredService;
import org.gridkit.coherence.integration.spring.service.MemberListenerCollection;
import org.gridkit.coherence.integration.spring.service.ServiceConfiguration;
import org.gridkit.coherence.integration.spring.service.ServiceListenerCollection;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.MemberListener;
import com.tangosol.net.Service;
import com.tangosol.net.management.Registry;
import com.tangosol.util.ServiceListener;

public class ClusteredServiceBean implements ClusteredService, InitializingBean, BeanNameAware, DisposableBean {

	protected String serviceName;
	protected String beanName;

	protected ServiceConfiguration configuration;
	protected boolean autostart = false;
	
	protected MemberListener memberListener;
	protected ServiceListener serviceListener;
	
	protected Service service;
	
	protected static ThreadUnlockHelper threadHelper = new ThreadUnlockHelper();
	
	public void setBeanName(String name) {
		this.beanName = name;
	}		

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Required
	public void setConfiguration(ServiceConfiguration config) {
		this.configuration = config;
	}
	
	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}

	public void setMemberListener(MemberListener listener) {
		this.memberListener = listener;
	}

	public void setServiceListener(ServiceListener listener) {
		this.serviceListener = listener;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (autostart) {
			ensureStarted();
		}
	}

	@Override
	public synchronized void destroy() throws Exception {
		if (service != null) {
			jmxUnregister(service);
			service.shutdown();			
		}
	}

	public Service getCoherenceService() {
		ensureStarted();
		return service;
	}
	
	private synchronized void ensureStarted() {
		if (this.service == null) {
			serviceName = serviceName == null ? beanName : serviceName;
			final Cluster cluster = CacheFactory.ensureCluster();
			final Service service = cluster.ensureService(serviceName, configuration.getServiceType().toString());
			synchronized(cluster) {
				if (!service.isRunning()) {
					service.configure(configuration.getXmlConfiguration());
					configureListener(service);
					configuration.postConfigure(service);
					initializeService(service);
				}
			}
			if (!service.isRunning()) {
				threadHelper.modalExecute(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized(cluster) {
							if (!service.isRunning()) {
								service.start();
							}
							return null;
						}   
					}
				});
			}
			validateService(service);
			this.service = service;
			jmxRegister(service);
		}
	}
	
	protected void configureListener(Service service) {
		if (memberListener != null) {
			if (memberListener instanceof MemberListenerCollection) {
				MemberListenerCollection mlc = (MemberListenerCollection) memberListener;
				for(MemberListener ml: mlc.getListeners()) {
					service.addMemberListener(ml);
				}
			}
			else {
				service.addMemberListener(memberListener);
			}
		}
		if (serviceListener != null) {
			if (serviceListener instanceof ServiceListenerCollection) {
				ServiceListenerCollection slc = (ServiceListenerCollection) serviceListener;
				for(ServiceListener sl: slc.getListeners()) {
					service.addServiceListener(sl);
				}
			}
			else {
				service.addServiceListener(serviceListener);
			}
		}
	}

	protected void jmxRegister(Service service) {
		String name = service.getInfo().getServiceName();
		Registry r = service.getCluster().getManagement();
		String id = "type=Service,name=" + name;
		id = r.ensureGlobalName(id);
		r.register(id, service);		
	}

	protected void jmxUnregister(Service service) {
		String name = service.getInfo().getServiceName();
		Registry r = service.getCluster().getManagement();
		String id = "type=Service,name=" + name;
		id = r.ensureGlobalName(id);
		r.unregister(id);		
	}

	protected void initializeService(Service srevice) {
		// do nothing
	}
	
	protected void validateService(Service service) {
		// do nothing
	}
}
