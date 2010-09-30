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

import com.tangosol.net.MemberEvent;
import com.tangosol.net.MemberListener;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface MemberListenerCollection {

	public MemberListener[] getListeners();
	
	public static class List implements MemberListener, MemberListenerCollection {
		
		private java.util.List<MemberListener> listeners = new ArrayList<MemberListener>();
		
		public List() {			
		};
		
		public List(Collection<?> list) {
			setList(list);
		}
		
		public void setList(Collection<?> list) {
			for(Object l: list) {
				MemberListener ml = (MemberListener) l;
				listeners.add(ml);
			}
		}

		@Override
		public MemberListener[] getListeners() {
			return listeners.toArray(new MemberListener[listeners.size()]);
		}

		@Override
		public void memberJoined(MemberEvent event) {
			for(MemberListener listener: listeners) {
				listener.memberJoined(event);
			}
		}

		@Override
		public void memberLeaving(MemberEvent event) {
			for(MemberListener listener: listeners) {
				listener.memberLeaving(event);
			}
		}

		@Override
		public void memberLeft(MemberEvent event) {
			for(MemberListener listener: listeners) {
				listener.memberLeft(event);
			}
		}
	}
}
