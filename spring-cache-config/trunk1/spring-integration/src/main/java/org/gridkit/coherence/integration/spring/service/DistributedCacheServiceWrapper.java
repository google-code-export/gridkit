/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

	@Override
	public void configure(XmlElement config) {
		service.configure(config);
		if (postProcessor != null) {
			postProcessor.postConfigure(service);
		}
	}
	
	// Delegates
	
	@Override
	public void addMemberListener(MemberListener arg0) {
		service.addMemberListener(arg0);
	}

	@Override
	public void addServiceListener(ServiceListener arg0) {
		service.addServiceListener(arg0);
	}

	@Override
	public void destroyCache(NamedCache arg0) {
		service.destroyCache(arg0);
	}

	@Override
	public NamedCache ensureCache(String arg0, ClassLoader arg1) {
		return service.ensureCache(arg0, arg1);
	}

	@Override
	public BackingMapManager getBackingMapManager() {
		return service.getBackingMapManager();
	}

	@Override
	public int getBackupCount() {
		return service.getBackupCount();
	}

	@Override
	public Enumeration<?> getCacheNames() {
		return service.getCacheNames();
	}

	@Override
	public Cluster getCluster() {
		return service.getCluster();
	}

	@Override
	public ClassLoader getContextClassLoader() {
		return service.getContextClassLoader();
	}

	@Override
	public ServiceInfo getInfo() {
		return service.getInfo();
	}

	@Override
	public KeyAssociator getKeyAssociator() {
		return service.getKeyAssociator();
	}

	@Override
	public Member getKeyOwner(Object arg0) {
		return service.getKeyOwner(arg0);
	}

	@Override
	public KeyPartitioningStrategy getKeyPartitioningStrategy() {
		return service.getKeyPartitioningStrategy();
	}

	@Override
	public PartitionSet getOwnedPartitions(Member arg0) {
		return service.getOwnedPartitions(arg0);
	}

	@Override
	public Set<?> getOwnershipEnabledMembers() {
		return service.getOwnershipEnabledMembers();
	}

	@Override
	public PartitionAssignmentStrategy getPartitionAssignmentStrategy() {
		return service.getPartitionAssignmentStrategy();
	}

	@Override
	public int getPartitionCount() {
		return service.getPartitionCount();
	}

	@Override
	public Serializer getSerializer() {
		return service.getSerializer();
	}

	@Override
	@SuppressWarnings("deprecation")
	public Set<?> getStorageEnabledMembers() {
		return service.getStorageEnabledMembers();
	}

	@Override
	public Object getUserContext() {
		return service.getUserContext();
	}

	@Override
	public boolean isLocalStorageEnabled() {
		return service.isLocalStorageEnabled();
	}

	@Override
	public boolean isRunning() {
		return service.isRunning();
	}

	@Override
	public void releaseCache(NamedCache arg0) {
		service.releaseCache(arg0);
	}

	@Override
	public void removeMemberListener(MemberListener arg0) {
		service.removeMemberListener(arg0);
	}

	@Override
	public void removeServiceListener(ServiceListener arg0) {
		service.removeServiceListener(arg0);
	}

	@Override
	public void setBackingMapManager(BackingMapManager arg0) {
		service.setBackingMapManager(arg0);
	}

	@Override
	public void setContextClassLoader(ClassLoader arg0) {
		service.setContextClassLoader(arg0);
	}

	@Override
	public void setUserContext(Object arg0) {
		service.setUserContext(arg0);
	}

	@Override
	public void shutdown() {
		service.shutdown();
	}

	@Override
	public void start() {
		service.start();
	}

	@Override
	public void stop() {
		service.stop();
	}

}
