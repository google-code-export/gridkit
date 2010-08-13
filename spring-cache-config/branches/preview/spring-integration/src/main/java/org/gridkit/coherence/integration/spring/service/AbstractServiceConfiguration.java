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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.tangosol.coherence.component.util.SafeService;
import com.tangosol.coherence.component.util.safeService.SafeCacheService;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.ProxyService;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

public abstract class AbstractServiceConfiguration implements ServiceConfiguration, ServicePostProcessor, Cloneable {
	
	public abstract ServiceType getServiceType();
		
	public XmlElement getXmlConfiguration() {
		XmlElement config = CacheFactory.getServiceConfig(getServiceType().toString());
		overrideServiceProperties(config);
		return config;		
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void overrideServiceProperties(XmlElement config) {
		for (Class<?> type = this.getClass(); type != Object.class; type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				XmlConfigProperty sp = field.getAnnotation(XmlConfigProperty.class);
				if (sp != null) {
					Object val = null;
					try {
						field.setAccessible(true);
						val = field.get(this);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					
					if (val != null) {
						config.ensureElement(sp.value()).setString(String.valueOf(val));
					}
				}
			}
		}
	}

	public void postConfigure(Service service) {
		if (service instanceof SafeService) {
			injectPostConfigureHook(service);
		}
		else  {
			try {
				overrideInstanceFields(service);
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize service", e);
			}
		}
	}

	void injectPostConfigureHook(Service service) {
		try {
			Field field = SafeService.class.getDeclaredField("__m_Service");
			field.setAccessible(true);
			
			Service cacheService = (Service) field.get(service);
			Service wrapper = wrap(cacheService, new ServicePostProcessor() {
				@Override
				public void postConfigure(Service service) {
					try {
						overrideInstanceFields(service);
					} catch (Exception e) {
						throw new RuntimeException("Failed to initialize service", e);
					}
				}
			});
			field.set(service, wrapper);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Service wrap(Service service, ServicePostProcessor servicePostProcessor) {
		if (service instanceof DistributedCacheService) {
			return new DistributedCacheServiceWrapper((DistributedCacheService) service, servicePostProcessor);
		}
		else if (service instanceof CacheService) {
			return new CacheServiceWrapper((CacheService) service, servicePostProcessor);
		}
		else if (service instanceof InvocationService) {
			return new InvocationServiceWrapper((InvocationService) service, servicePostProcessor);
		}
		else if (service instanceof ProxyService) {
			return new ProxyServiceWrapper((ProxyService) service, servicePostProcessor);
		}
		else {
			throw new IllegalArgumentException("Unexpected service type: " + service.getClass().getName());
		}
	}

	protected void overrideInstanceFields(Service service) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		for (Class<?> type = this.getClass(); type != Object.class; type = type.getSuperclass()) {
			for (Field field : type.getDeclaredFields()) {
				ReflectionInjectedProperty sp = field.getAnnotation(ReflectionInjectedProperty.class);
				if (sp != null) {
					Object val = null;
					try {
						field.setAccessible(true);
						val = field.get(this);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					
					if (val != null) {
						String path = sp.value();
						SetAccesser accesser = resolveField(service, path);
						accesser.set(val);
					}
				}
			}
		}
	}
	
	private SetAccesser resolveField(Service service, String path) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Object target = service;
		String pr = path;
		if (service instanceof SafeCacheService) {
			pr = "__m_Service." + pr;
		}
		while(pr.indexOf('.') >= 0) {
			int p = pr.indexOf('.');
			String segment = pr.substring(0, p);
			pr = pr.substring(p + 1);
//			if (segment.endsWith("()")) {
//				segment = segment.substring(0, segment.length() - 2);
//				Method m = target.getClass().getMethod(segment, new Class[0]);
//				m.setAccessible(true);
//				target = m.invoke(target);
//			}
//			else 
			{
				Field field = getField(target, segment);
				field.setAccessible(true);
				target = field.get(target);
			}
		}

		Field field = getField(target, pr);
		field.setAccessible(true);
		
		return new SetAccesser(target, field);		
	}

	private Field getField(Object target, String name) throws NoSuchFieldException {
		Class<?> type = target.getClass();
		while(type != Object.class) {
			try {
				Field field = type.getDeclaredField(name);
				return field;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}	
	
	private class SetAccesser {
		private final Object object;
		private final Field field;

		public SetAccesser(Object object, Field field) {
			this.object = object;
			this.field = field;
		}
		
		public void set(Object value) throws IllegalArgumentException, IllegalAccessException {
			field.set(object, value);
		}		
	}
}
