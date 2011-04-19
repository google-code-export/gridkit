/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class RehashDaemon implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(RehashDaemon.class);
    
    private static ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(4, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread();
			t.setName("RehashDaemon");
			t.setDaemon(true);
			return t;
		}
	});

    private WeakReference<PagedLinearHashStore<?>> hashtableRef;
    private int checkInterval;
    private boolean active;
    private int lastSize = -1;
    
    public RehashDaemon(PagedLinearHashStore<?> hashtable, int checkInterval) {
        this.hashtableRef = new WeakReference<PagedLinearHashStore<?>>(hashtable);
        this.checkInterval = checkInterval;
    }

    @Override
    public synchronized void run() {
        PagedLinearHashStore<?> hashtable = hashtableRef.get();
        if (hashtable != null) {
            int delay = checkInterval;
            int expectedTableSize = hashtable.size() / hashtable.getTargetPageSize();
            if (hashtable.getTableSize() < expectedTableSize) {
                int delta = hashtable.size() / hashtable.getTargetPageSize() - hashtable.getTableSize();
                logger.trace("Hashtable(@" + hashtable.hashCode() + "): Rehashing pageSize=" + hashtable.getAveragePageSize() + ", tableSize=" + hashtable.getTableSize() + ", delta=" + delta);
                if (delta > 0) {
                    try {
                        hashtable.growTable(delta);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (lastSize != hashtable.size()) {
                lastSize = hashtable.size();
                logger.debug("Hashtable(@" + hashtable.hashCode() + "): Size " + lastSize);
            }
            if (active) {
                EXECUTOR.schedule(this, delay, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    public synchronized void start() {
        if (!active) {
            active = true;
            EXECUTOR.schedule(this, checkInterval, TimeUnit.MILLISECONDS);            
        }
    }
    
    public synchronized void stop() {
        active = false;
    }
}
