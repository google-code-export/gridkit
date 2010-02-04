/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class BenchmarkSupport {

	static class ExecReportSupport {
		static BlockingQueue<ExecMarkEnvelop> queue = new ArrayBlockingQueue<ExecMarkEnvelop>(1024);
		static {
			Thread pump = new Thread() {
				@Override
				public void run() {
					pumpQueue();
				}
			};
			pump.setDaemon(true);
			pump.setName("ExecReportPump");
			pump.start();
		}
		
		static void pumpQueue() {
			try {
				while(true) {
					List<ExecMarkEnvelop> list = new ArrayList<ExecMarkEnvelop>();
					list.add(queue.take());
					queue.drainTo(list);
					
					Map<String, Map<Long, CommandExecutionMark>> buf = new HashMap<String, Map<Long,CommandExecutionMark>>();
					
					for(ExecMarkEnvelop env: list) {
						Map<Long, CommandExecutionMark> map = buf.get(env.cacheName);
						if (map == null) {
							buf.put(env.cacheName, map = new HashMap<Long, CommandExecutionMark>());
						}
						map.put(env.mark.getExecutionID(), env.mark);
					}
					
					for(Map.Entry<String, Map<Long, CommandExecutionMark>> entry: buf.entrySet()) {
						NamedCache cache = CacheFactory.getCache(entry.getKey());
						cache.putAll(entry.getValue());
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	static class ExecMarkEnvelop {
		String cacheName;
		CommandExecutionMark mark;
		
		public ExecMarkEnvelop(String cacheName, CommandExecutionMark mark) {
			this.cacheName = cacheName;
			this.mark = mark;
		}
	}
	
	public static void reportExecution(String buffer, CommandExecutionMark mark) {
		try {
			ExecReportSupport.queue.put(new ExecMarkEnvelop(buffer, mark));
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	@SuppressWarnings("unchecked")
	public static Map waitForBuffer(String cache, long totalSize) {
		return waitForBuffers(Collections.singletonList(cache), totalSize);
	}
	
	@SuppressWarnings("unchecked")
	public static Map waitForBuffers(Collection<String> cacheList, long totalSize) {
		NamedCache[] caches = new NamedCache[cacheList.size()];
		int n = 0;
		for(String name: cacheList) {
			caches[n++] = CacheFactory.getCache(name);
		}
		
		while(true) {
			int size = 0;
			for(NamedCache cache: caches) {
				size += cache.size();
			}
			
			if (size >= totalSize) {
				break;
			}
			
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(5));
		}
		
		Map buffer = new HashMap();

		for(NamedCache cache: caches) {
			Map map = new HashMap(cache);
			cache.keySet().removeAll(map.keySet());
			buffer.putAll(map);
		}
		
		return buffer;
	}
	
}
