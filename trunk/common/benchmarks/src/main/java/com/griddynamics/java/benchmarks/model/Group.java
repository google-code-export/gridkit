package com.griddynamics.java.benchmarks.model;

import com.griddynamics.java.benchmarks.engine.TickCounter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * typing lifeTime for Entity
 * User: akondratyev
 * Date: Dec 16, 2010
 * Time: 6:58:15 PM
 */
public class Group {
    private static int count = 0;
    private int id = count++;
    private int maxObjectsCount;
    private AtomicInteger objectsCount = new AtomicInteger(0);             
    private int objectLifeTime;
    private int objectSize;
    private Storage objectsStorage;

    private Group(Builder builder) {
        this.maxObjectsCount = builder.maxObjectsCount;
        this.objectLifeTime = builder.objectLifeTime;
        this.objectSize = builder.objectSize;
        objectsStorage = new Storage(this, objectLifeTime, builder.tickCounter);
    }

    public static class Builder {
        private int maxObjectsCount;
        private int objectLifeTime;
        private int objectSize;
        private TickCounter tickCounter;

        public Builder setMaxObjectsCount(int maxObjectsCount) {
            this.maxObjectsCount = maxObjectsCount;
            return this;
        }

        public Builder setObjectLifeTime(int objectLifeTime) {
            this.objectLifeTime = objectLifeTime;
            return this;
        }

        public Builder setObjectSize(int objectSize) {
            this.objectSize = objectSize;
            return this;
        }

        public Builder setTickCounter(TickCounter tickCounter) {
            this.tickCounter = tickCounter;
            return this;
        }

        public Group build() {
            return new Group(this);
        }
    }

    /*public Group(int objectLifeTime, int objectSize) {
        this.objectLifeTime = objectLifeTime;
        objectsStorage = new Storage(this, objectLifeTime, tickCounter);
        this.objectSize = objectSize;
    }
*/
    public int getObjectLifeTime() {
        return objectLifeTime;
    }

    public int getObjectSize() {
        return objectSize;
    }

    public int getId() {
        return id;
    }

    public int getMaxObjectsCount() {
        return maxObjectsCount;
    }

    public void setMaxObjectsCount(int maxObjectsCount) {
        this.maxObjectsCount = maxObjectsCount;
    }

    public void incObjectsCount() {
        objectsCount.getAndIncrement();
    }

    public void reclaimObjects(int deletedObjectCount) {  
        objectsCount.getAndAdd(-deletedObjectCount);
    }

    public int getObjectsCount() {
        return objectsCount.get();
    }

    public Storage getStorage() {
        return objectsStorage;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", maxObjectsCount=" + maxObjectsCount +
                ", objectLifeTime=" + objectLifeTime +
                ", objectSize=" + objectSize +
                '}';
    }
}
