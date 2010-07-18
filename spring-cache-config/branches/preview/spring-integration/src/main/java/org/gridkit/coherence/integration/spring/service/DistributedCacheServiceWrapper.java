package org.gridkit.coherence.integration.spring.service;

import java.util.Enumeration;
import java.util.Set;

import com.tangosol.io.Serializer;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.Cluster;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.Member;
import com.tangosol.net.MemberListener;
import com.tangosol.net.NamedCache;
import com.tangosol.net.ServiceInfo;
import com.tangosol.net.partition.KeyAssociator;
import com.tangosol.net.partition.KeyPartitioningStrategy;
import com.tangosol.net.partition.PartitionAssignmentStrategy;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ServiceListener;

class DistributedCacheServiceWrapper implements DistributedCacheService {
	
	private final DistributedCacheService service;
	private final ServicePostProcessor postProcessor;

	public DistributedCacheServiceWrapper(DistributedCacheService service, ServicePostProcessor postProcessor) {
		this.service = service;
		this.postProcessor = postProcessor;
	}

	public void configure(XmlElement config) {
		service.configure(config);
		if (postProcessor != null) {
			postProcessor.postConfigure(service);
		}
	}
	
	// Delegates
	
	public void addMemberListener(MemberListener arg0) {
		service.addMemberListener(arg0);
	}

	public void addServiceListener(ServiceListener arg0) {
		service.addServiceListener(arg0);
	}

	public void destroyCache(NamedCache arg0) {
		service.destroyCache(arg0);
	}

	public NamedCache ensureCache(String arg0, ClassLoader arg1) {
		return service.ensureCache(arg0, arg1);
	}

	public BackingMapManager getBackingMapManager() {
		return service.getBackingMapManager();
	}

	public int getBackupCount() {
		return service.getBackupCount();
	}

	public Enumeration<?> getCacheNames() {
		return service.getCacheNames();
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

	public KeyAssociator getKeyAssociator() {
		return service.getKeyAssociator();
	}

	public Member getKeyOwner(Object arg0) {
		return service.getKeyOwner(arg0);
	}

	public KeyPartitioningStrategy getKeyPartitioningStrategy() {
		return service.getKeyPartitioningStrategy();
	}

	public PartitionSet getOwnedPartitions(Member arg0) {
		return service.getOwnedPartitions(arg0);
	}

	public Set<?> getOwnershipEnabledMembers() {
		return service.getOwnershipEnabledMembers();
	}

	public PartitionAssignmentStrategy getPartitionAssignmentStrategy() {
		return service.getPartitionAssignmentStrategy();
	}

	public int getPartitionCount() {
		return service.getPartitionCount();
	}

	public Serializer getSerializer() {
		return service.getSerializer();
	}

	@SuppressWarnings("deprecation")
	public Set<?> getStorageEnabledMembers() {
		return service.getStorageEnabledMembers();
	}

	public Object getUserContext() {
		return service.getUserContext();
	}

	public boolean isLocalStorageEnabled() {
		return service.isLocalStorageEnabled();
	}

	public boolean isRunning() {
		return service.isRunning();
	}

	public void releaseCache(NamedCache arg0) {
		service.releaseCache(arg0);
	}

	public void removeMemberListener(MemberListener arg0) {
		service.removeMemberListener(arg0);
	}

	public void removeServiceListener(ServiceListener arg0) {
		service.removeServiceListener(arg0);
	}

	public void setBackingMapManager(BackingMapManager arg0) {
		service.setBackingMapManager(arg0);
	}

	public void setContextClassLoader(ClassLoader arg0) {
		service.setContextClassLoader(arg0);
	}

	public void setUserContext(Object arg0) {
		service.setUserContext(arg0);
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
