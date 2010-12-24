package com.griddynamics.java.benchmarks.engine;

import com.griddynamics.java.benchmarks.model.Entity;
import com.griddynamics.java.benchmarks.model.Group;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * class for creating objects for specified group and pushes its to storage
 * User: akondratyev
 * Date: Dec 16, 2010
 * Time: 7:23:54 PM
 */
public class EntityCreator implements Runnable {

    public static Logger logger = LogManager.getLogger(EntityCreator.class);

    private ConcurrentLinkedQueue<Group> groups;
    private ConcurrentLinkedQueue<Integer> ticks = new ConcurrentLinkedQueue<Integer>();
    private TickCounter ticker;

    public EntityCreator(ConcurrentLinkedQueue<Group> groups, TickCounter ticker) {
        this.groups = groups;
        this.ticker = ticker;
        ticker.addThreadTickQueue(ticks);
    }

    public void run() {
        while (true) {
            Group currentGr = groups.poll();

            if (currentGr == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info(Thread.currentThread().getName() + "  didn't get any group..");
                continue;
            }
            if (currentGr.getObjectsCount() >= currentGr.getMaxObjectsCount()) {
                logger.info("created max objects (" + currentGr.getObjectsCount()
                        + ") for group " + currentGr.getId());
                //continue;         //todo: we just return group to the queue
            } else {
                logger.debug("count of created objs is " + currentGr.getObjectsCount());
                Entity entity = new Entity();
                entity.setObjectSizeKb(currentGr.getObjectSize());
                currentGr.getStorage().push(ticker.getTicks(), entity);
                currentGr.incObjectsCount();
                if (!ticks.offer(1))
                    throw new UnsupportedOperationException("can't insert into tick queue...");
            }
            if (!groups.offer(currentGr))
                throw new UnsupportedOperationException("cannot return current group to queue: ");
        }
    }
}
