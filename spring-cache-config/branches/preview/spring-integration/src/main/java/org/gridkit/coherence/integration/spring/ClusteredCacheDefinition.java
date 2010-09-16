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

package org.gridkit.coherence.integration.spring;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import com.tangosol.net.BackingMapManagerContext;

public class ClusteredCacheDefinition extends CacheDefinition {

	private String backendBeanId;
	private Object backendBean;
	
	public ClusteredCacheDefinition() {
	}
	
	@Required
	public void setBackTier(Object back) {
		if (back instanceof String) {
			backendBeanId = (String) back;
		}
		else {
			backendBean = (Map<?, ?>) back;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if ((backendBean == null) && (backendBeanId == null)) {
			throw new IllegalArgumentException("No backing map is configured");
		}
		if (backendBean != null) {
			// TODO validation
			validateBackendBean();
		}
		initGate.countDown();
	}

	@SuppressWarnings("unchecked")
	private void validateBackendBean() {
		if (backendBean instanceof Map) {
			return;
		}
		if (backendBean instanceof MapProvider) {
			return;
		}
		if (backendBean instanceof BackningMapProvider) {
			return;
		}
		throw new IllegalArgumentException("Invalid type for backendBean " + backendBean.getClass().getName() + ", should be Map, MapProvider or BackingMapProvider");
	}

	public Map<?, ?> getBackendInstance(ApplicationContext appCtx, BackingMapManagerContext cacheCtx) {
		// should wait bean to be initialized
		try {
			initGate.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
//		if (!initialized) {
//			System.out.println("By thread " + threadName);
//			callSite.printStackTrace();
//			throw new IllegalStateException("Cache definition is not initialized");
//		}
		if (backendBean != null) {
			return resolveMap(backendBean, cacheCtx);
		}
		else {
			return resolveMap(appCtx.getBean(backendBeanId), cacheCtx);
		}
	}

	private Map<?, ?> resolveMap(Object bean, BackingMapManagerContext cacheCtx) {
		Map<?, ?> map = BackningMapProvider.Helper.getMapFromBean(bean, cacheCtx, Map.class);
		return map;
	}
}
