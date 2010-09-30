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

import org.gridkit.coherence.integration.spring.CacheLookupStrategy;

import com.tangosol.coherence.component.net.extend.proxy.serviceProxy.CacheServiceProxy;
import com.tangosol.net.NamedCache;
import com.tangosol.net.WrapperCacheService;

/**
 * Wrapper for the cache service proxy inside ProxyService
 * @author malexejev@gmail.com
 * 27.09.2010
 */
public class CacheServiceProxyWrapper extends WrapperCacheService {
	
	private static final long serialVersionUID = 1L;
	
	private final CacheLookupStrategy cacheLookupStrategy;
	
	public CacheServiceProxyWrapper(CacheServiceProxy delegate,
			CacheLookupStrategy cacheLookupStrategy) {
		super(delegate);
		this.cacheLookupStrategy = cacheLookupStrategy;
	}

	@Override
	public void destroyCache(NamedCache cache) {
		cacheLookupStrategy.destroyCache(cache);
	}

	@Override
	public NamedCache ensureCache(String sName, ClassLoader loader) {
		NamedCache cache = cacheLookupStrategy.ensureCache(sName, loader);
		/*
		 * FIXME: implement this stuff borrowed from CacheServiceProxy.ensureCache
		 * 
		Serializer serializerThis = getSerializer();
		Serializer serializerThat = ((CacheServiceProxy) getService()).getSerializer(cache);
		if ((ExternalizableHelper.isSerializerCompatible(serializerThis, serializerThat))) {
			this.releaseCache(cache);
			cache = cacheLookupStrategy.ensureCache(sName, loader);
			cache = ConverterCollections.getNamedCache(cache, 
					((CacheServiceProxy) getService()).getConverterToBinary(), 
					((CacheServiceProxy) getService()).getConverterFromBinary(), 
					((CacheServiceProxy) getService()).getConverterToBinary(), 
					((CacheServiceProxy) getService()).getConverterFromBinary());
			
			if (((CacheServiceProxy) getService()).getNamedCacheSet().add(sName)) {
				if ((serializerThat != null ? 0 : 1) != 0) {
					Component._trace("The cache \"" + sName + "\" does not support" + " pass-through optimization for objects in" + " internal format. If possible, consider using" + " a different cache topology.", 3);
				} else {
					ExternalizableHelper.reportIncompatibleSerializers(cache, ((CacheServiceProxy) getService()).getServiceName(), serializerThis);
				}
			}
		}
		*/
		return cache;
	}
	
}
