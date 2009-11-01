/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package com.griddynamics.gridkit.coherence.benchmark.event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class EventSource {
	
	private final static ExecutorService EXECUTORS = Executors.newCachedThreadPool();
	
	
	public static void main(String[] args) {
		try {
			
			System.setProperty("tangosol.coherence.distributed.localstorage", "false");
			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");
			
			final NamedCache out = CacheFactory.getCache("out-pool");
			final NamedCache in = CacheFactory.getCache("in-pool");
			
			out.clear();
			in.clear();
			
			long objectCount = 100000;
			int threadCount = 20;
			
			final Object value = new String[]{"ABC", "EFG", "123", "890", "QWERTY"};
			
			ExecutorService service = Executors.newFixedThreadPool(threadCount);

			Thread.sleep(500);
			
            System.out.println("Start test");
            
            long startTimestamp = System.nanoTime();
            for(long i = 0; i != objectCount; ++i) {
            	final Object key = Long.valueOf(i);
            	service.execute(new Runnable() {
					@Override
					public void run() {
						out.put(key, value);
					}
				});
            }
            
            while(true) {
            	if (in.size() == objectCount) {
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
