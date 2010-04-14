/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package com.griddynamics.gridkit.coherence.benchmark.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class EventSource3 {
	
	private final static ExecutorService EXECUTORS = Executors.newCachedThreadPool();
	
	
	public static void main(String[] args) {
		try {
			
			System.setProperty("tangosol.coherence.distributed.localstorage", "false");
			System.setProperty("tangosol.coherence.cacheconfig", "event-benchmark-cache-config.xml");
			
			final NamedCache p1 = CacheFactory.getCache("pool-1");
			final NamedCache p2 = CacheFactory.getCache("pool-2");
			final NamedCache p3 = CacheFactory.getCache("pool-3");
			final NamedCache p4 = CacheFactory.getCache("pool-4");
			
			p1.clear();
			p2.clear();
			p3.clear();
			p4.clear();
			
			long objectCount = 50000;
			final int batchSize = 4;
			int threadCount = 1;
			
			final Object value = new String[]{"ABC", "EFG", "123", "890", "QWERTY"};
			
			startPooler(p1, p2);
			startPooler(p2, p3);
			startPooler(p3, p4);

			ExecutorService service = Executors.newFixedThreadPool(threadCount);
			
			Thread.sleep(500);
			
            System.out.println("Start test");
            
            long startTimestamp = System.nanoTime();
            for(long i = 0; i != objectCount; i += batchSize) {
            	final Long key = Long.valueOf(i);
            	service.execute(new Runnable() {
					@Override
					public void run() {
						if (batchSize == 1) {
							p1.put(key, value);
						}
						else {
							Map<Long, Object> batch = new HashMap<Long, Object>();
							for(int j = 0; j != batchSize; ++j) {
								batch.put(key + j, value);
							}
							p1.putAll(batch);
						}
					}
				});
            }
            
            service.shutdown();
            service.awaitTermination(60, TimeUnit.SECONDS);
            
            System.out.println("Wait for cache");
            
            while(true) {
            	if (p4.size() == objectCount) {
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
	}

	private static void startPooler(final NamedCache src, final NamedCache dst) {
		Thread poller = new Thread(){
			@Override
			public void run() {
				while(true) {
					Map buf = new HashMap();
					for(Object next: src.entrySet()) {
						Map.Entry entry = (Entry) next;
						buf.put(entry.getKey(), entry.getValue());
					}
					if (buf.isEmpty()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else {
						dst.putAll(buf);
						src.keySet().removeAll(buf.keySet());
					}
				}				
			}
		};
		
		poller.setDaemon(true);
		poller.start();
	};

}
