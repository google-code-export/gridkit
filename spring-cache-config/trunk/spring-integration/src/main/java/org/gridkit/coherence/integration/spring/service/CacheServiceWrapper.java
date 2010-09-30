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

import com.tangosol.io.Serializer;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.CacheService;
import com.tangosol.net.Cluster;
import com.tangosol.net.MemberListener;
import com.tangosol.net.NamedCache;
import com.tangosol.net.ServiceInfo;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.util.ServiceListener;

class CacheServiceWrapper implements CacheService {

	private final CacheService service;
	private final ServicePostProcessor postProcessor;

	public CacheServiceWrapper(CacheService service, ServicePostProcessor postProcessor) {
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

	// delegates
	
	@Override
	public void addMemberListener(MemberListener paramMemberListener) {
		service.addMemberListener(paramMemberListener);
	}

	@Override
	public void addServiceListener(ServiceListener paramServiceListener) {
		service.addServiceListener(paramServiceListener);
	}

	@Override
	public void destroyCache(NamedCache paramNamedCache) {
		service.destroyCache(paramNamedCache);
	}

	@Override
	public NamedCache ensureCache(String paramString,
			ClassLoader paramClassLoader) {
		return service.ensureCache(paramString, paramClassLoader);
	}

	@Override
	public BackingMapManager getBackingMapManager() {
		return service.getBackingMapManager();
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
	public Serializer getSerializer() {
		return service.getSerializer();
	}

	@Override
	public Object getUserContext() {
		return service.getUserContext();
	}

	@Override
	public boolean isRunning() {
		return service.isRunning();
	}

	@Override
	public void releaseCache(NamedCache paramNamedCache) {
		service.releaseCache(paramNamedCache);
	}

	@Override
	public void removeMemberListener(MemberListener paramMemberListener) {
		service.removeMemberListener(paramMemberListener);
	}

	@Override
	public void removeServiceListener(ServiceListener paramServiceListener) {
		service.removeServiceListener(paramServiceListener);
	}

	@Override
	public void setBackingMapManager(BackingMapManager paramBackingMapManager) {
		service.setBackingMapManager(paramBackingMapManager);
	}

	@Override
	public void setContextClassLoader(ClassLoader paramClassLoader) {
		service.setContextClassLoader(paramClassLoader);
	}

	@Override
	public void setUserContext(Object paramObject) {
		service.setUserContext(paramObject);
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
