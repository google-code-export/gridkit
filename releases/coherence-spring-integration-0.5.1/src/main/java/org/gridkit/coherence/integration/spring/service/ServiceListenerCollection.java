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

import java.util.ArrayList;
import java.util.Collection;

import com.tangosol.util.ServiceEvent;
import com.tangosol.util.ServiceListener;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ServiceListenerCollection {

	public ServiceListener[] getListeners();
	
	public static class List implements ServiceListener, ServiceListenerCollection {
		
		private java.util.List<ServiceListener> listeners = new ArrayList<ServiceListener>();
		
		public List() {			
		};
		
		public List(Collection<?> list) {
			setList(list);
		}
		
		public void setList(Collection<?> list) {
			for(Object l: list) {
				ServiceListener ml = (ServiceListener) l;
				listeners.add(ml);
			}
		}

		@Override
		public ServiceListener[] getListeners() {
			return listeners.toArray(new ServiceListener[listeners.size()]);
		}

		@Override
		public void serviceStarting(ServiceEvent event) {
			for(ServiceListener l: listeners) {
				l.serviceStarting(event);
			}
		}
		
		@Override
		public void serviceStarted(ServiceEvent event) {
			for(ServiceListener l: listeners) {
				l.serviceStarted(event);
			}
		}

		@Override
		public void serviceStopping(ServiceEvent event) {
			for(ServiceListener l: listeners) {
				l.serviceStopping(event);
			}
		}

		@Override
		public void serviceStopped(ServiceEvent event) {
			for(ServiceListener l: listeners) {
				l.serviceStopped(event);
			}
		}		
	}
}
