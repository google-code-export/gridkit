package com.griddynamics.java.benchmarks.engine;

import com.griddynamics.java.benchmarks.model.event.TickEvent;
import com.griddynamics.java.benchmarks.model.event.TickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: akondratyev
 * Date: Dec 17, 2010
 * Time: 12:51:13 PM
 */
public class TickCounter {

    private static Logger logger = LogManager.getLogger(TickCounter.class);

    private volatile int ticks = 0;
    private List<TickListener> listeners = new ArrayList<TickListener>();
    private CopyOnWriteArrayList<ConcurrentLinkedQueue<Integer>> threadsTickQueueList = new CopyOnWriteArrayList<ConcurrentLinkedQueue<Integer>>();        //todo: queue type?
    private Thread tickCollector;
    private ExecutorService storageCleaner = Executors.newFixedThreadPool(1000);


    public TickCounter() {
        tickCollector = new Thread(new TickCollector());
        tickCollector.setDaemon(true);
        tickCollector.start();
    }

    public void addTickEventListener(TickListener listener) {
        listeners.add(listener);
    }

    public int getTicks() {
        return ticks;
    }

    public void setThreadsTickQueueList(CopyOnWriteArrayList<ConcurrentLinkedQueue<Integer>> threadsTickQueueList) {
        this.threadsTickQueueList = threadsTickQueueList;
    }

    public void addThreadTickQueue(ConcurrentLinkedQueue<Integer> tickQueue) {
        threadsTickQueueList.add(tickQueue);
    }

    private class TickCollector implements Runnable {
        private boolean stop = false;   //todo: maybe must be volatile?

        public void run() {
            while (!stop) {
                for (int i = 0; i < threadsTickQueueList.size(); i++) {
                    if (threadsTickQueueList.get(i).poll() != null){
                        incrementTick();
                        logger.debug("current tick is " + ticks);
                    }
                }
            }
        }

        public void stopped() {
            stop = true;
        }
    }

    private class StorageCleanTask implements Runnable {
        private List<TickListener> storagesToClean;
        private int currentTick;

        public StorageCleanTask(List<TickListener> storagesToClean, int tick) {
            this.storagesToClean = storagesToClean;
            this.currentTick = tick;
        }

        public void run() {
            for (TickListener listener : storagesToClean) {
                if ((ticks % listener.getFactor()) == 0) {
                    logger.debug("send tickHappend for " + listener + " on " + ticks + " tick");
                    listener.tickHappened(new TickEvent(TickCounter.class, currentTick));
                }
            }
        }
    }

    /**
     * don't use as public, just for test
     * */
    public void incrementTick() {
        ticks++;
    }
}
