/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.benchmark.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;

public class PushTask implements Serializable, Invocable{

	private Object object;
	private int objectCount;
	private int threadCount;
	private int bacthSize;
	private String cacheName;
	private transient NamedCache cache;
	
	public PushTask(Object object, int objectCount, int threadCount, int bacthSize, String cacheName) {
		this.object = object;
		this.objectCount = objectCount;
		this.threadCount = threadCount;
		this.bacthSize = bacthSize;
		this.cacheName = cacheName;
	}

	@Override
	public void init(InvocationService service) {
		cache = CacheFactory.getCache(cacheName);
	}

	@Override
	public Object getResult() {
		return null;
	}
	
	@Override
	public void run() {
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);
		
		for(int i = 0; i <= objectCount; i += bacthSize) {
			final int start = i;
			final int finish = i + bacthSize > objectCount ? objectCount : i + bacthSize;
			
			exec.execute(new Runnable() {
				@Override
				public void run() {
					Map buf = new HashMap();
					for(int j = start; j != finish; ++j) {
						buf.put(j, object);
					}
					cache.putAll(buf);
				}
			});
		}
		
		exec.shutdown();
		try {
			exec.awaitTermination(3, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
