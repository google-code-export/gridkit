package com.griddynamics.java.benchmarks.model;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;

import com.griddynamics.java.benchmarks.engine.TickCounter;

/**
 * use to store groups of objects with certain id
 * User: akondratyev
 * Date: Dec 16, 2010
 * Time: 6:58:22 PM
 */
public class Storage {

    private static Logger logger = LogManager.getLogger(Storage.class);

    private int objectsLifeTime;
    private int factor = 100;     //todo: set by property file
    private ConcurrentMap<Integer, List<Entity>> storage = new ConcurrentHashMap<Integer, List<Entity>>();
    private Queue<Integer> storageKeyQueue = new LinkedList<Integer>();
    private Group myGroup;
    private TickCounter tickCounter;

    public Storage(Group myGroup, int objectsLifeTime, TickCounter tickCounter) {
        this.myGroup = myGroup;
        this.objectsLifeTime = objectsLifeTime;
        this.tickCounter = tickCounter;
        Thread cleaner = new Thread(new Cleaner());
        cleaner.setDaemon(true);
        cleaner.start();
    }

    private class Cleaner implements Runnable {
        public void run() {
            while (true) {
                int theOldestLivedKey = (tickCounter.getTicks() - objectsLifeTime) / factor;
                while (storageKeyQueue.peek() != null && storageKeyQueue.peek() <= theOldestLivedKey) {
                    int key = storageKeyQueue.poll();
                    logger.debug("remove key " + key);
                    logger.debug("reclaimed " + storage.get(key).size() + " objects");
                    myGroup.reclaimObjects(storage.remove(key).size());
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void push(int currentTick, Entity entity) {
        int key = currentTick / factor;
        if (!storage.containsKey(key)) {
            storage.put(key, new LinkedList<Entity>());
            storageKeyQueue.offer(key);
            logger.debug("create new chunk for key " + key);
        }
        storage.get(key).add(entity);
        logger.debug("put entity " + entity + " into storage");
    }

    public List<Entity> getEntity(int key) {
        return storage.get(key);
    }

    public void setFactor(int factor) {
        this.factor = factor;
    }

    public Map<Integer, List<Entity>> getBackMap() {
        return storage;
    }
    /*
    public long getFactor() {
        return factor;
    }

    public void tickHappened(TickEvent tick) {
        long currentTick = tick.getCurrentTick();
        System.out.println("currentTick is " + currentTick);
        logger.debug("try to delete objects with lifetime less than tick " + currentTick);
        if ((currentTick - objectsLifeTime) > 0)              
            for (int i = (int) ((currentTick - objectsLifeTime) / factor); i >= 0; i--) {
                System.out.println("remove object with index i " + i);
                if (storage.remove(i) == null)
                    return;
            }
    }*/

    /*public static void main(String[] args) throws Exception {

        Storage st = new Storage(null, 100, tickCounter);
        st.setFactor(10);
        for (int i = 0; i < 200; i++) {
            Entity entity = new Entity();
            entity.setObjectSizeKb(1024);
            st.push(i, entity);
        }
        System.out.println("size " + st.getBackMap().size());
    }*/
}
