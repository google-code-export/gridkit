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
package com.griddynamics.gridkit.coherence.benchmark.event.pingpong;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.benchmark.event.PushTask;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;

public class PingPongControler {

	public static void main(String[] args) {
		try {
			
			System.setProperty("tangosol.coherence.distributed.localstorage", "false");
			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");
			
			// ***
			System.setProperty("event-benchmark-thread-count", "4");
			
			NamedCache out = CacheFactory.getCache("out-pool");
			NamedCache in = CacheFactory.getCache("in-pool");
			
			out.clear();
			in.clear();
			
			// ***
			int objectCount = 100000;			
			// ***
			int threadCount = 4;
			// ***
			int bacthSize = 1;
			
			// ***
			Object value = new String[]{"ABC", "EFG", "123", "890", "QWERTY"};
			
			PushTask task = new PushTask(value, objectCount, threadCount, bacthSize, "in-pool");
			
			InvocationService rc = (InvocationService) CacheFactory.getService("event-remote-control-service");
			
			Set members = rc.getInfo().getServiceMembers();
			members.remove(CacheFactory.getCluster().getLocalMember());
			
            System.out.println("Start test");
			long startTimestamp = System.nanoTime();

			rc.execute(task, members, null);

            while(true) {
            	if (out.size() == objectCount) {
            		break;
            	}
            	else {
            		Thread.sleep(1);
            	}
            }
            
            long finishTimestamp = System.nanoTime();

            double time = (double) (finishTimestamp - startTimestamp) / TimeUnit.SECONDS.toNanos(1);
            double throughput = objectCount / time;
            
            System.out.println("Execution time " + time + " ms");
            System.out.println("Object count " + objectCount);
            System.out.println("Throughput " + throughput + " op/sec");
            
            System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	};
}
