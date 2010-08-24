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

import com.tangosol.net.BackingMapManagerContext;

public interface BackningMapProvider {
	public Map<?, ?> getBackinMap(BackingMapManagerContext context);
	
	public static class Helper {

		@SuppressWarnings("unchecked")
		public static <T extends Map> T getMapFromBean(Object bean, BackingMapManagerContext cacheCtx, Class<T> mapType) {
			Map map;
			if (bean == null) {
				throw new NullPointerException("Bean object is null");
			}
			else if (bean instanceof Map) {
				map = (Map<?, ?>) bean;
			}
			else if (bean instanceof MapProvider) {
				map = ((MapProvider) bean).getMap();
			}
			else if (bean instanceof BackningMapProvider) {
				map = ((BackningMapProvider) bean).getBackinMap(cacheCtx);
			}
			else {
				throw new IllegalArgumentException("Invalid type for backendBean " + bean.getClass().getName() + ", should be Map, MapProvider or BackingMapProvider");
			}
			
			if (mapType != null) {
				if (!mapType.isInstance(map)) {
					throw new IllegalArgumentException("Provided map object of class " + map.getClass().getName() + " is not compatible with " + mapType.getName());
				}
				return mapType.cast(map);
			}
			else {
				return (T)map;
			}
		}
	}
}
