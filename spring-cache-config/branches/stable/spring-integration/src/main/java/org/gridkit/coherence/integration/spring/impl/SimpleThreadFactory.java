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

package org.gridkit.coherence.integration.spring.impl;

import java.util.concurrent.ThreadFactory;

class SimpleThreadFactory implements ThreadFactory {

	private final String name;
	private final ThreadGroup group;
	private final boolean daemon;
	
	public SimpleThreadFactory(ThreadGroup group, String name, boolean daemon) {
		this.group = group;
		this.name = name;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread;
		if (group != null) {
			thread = new Thread(group, r);
		}
		else {
			thread = new Thread(r);
		}

		if (name != null) {
			thread.setName(name);
		}
		
		thread.setDaemon(daemon);
					
		return thread;
	}
}
