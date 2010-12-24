package com.griddynamics.java.benchmarks.model.event;

import java.util.EventObject;

/**
 * User: akondratyev
 */
public class DataReclaimed extends EventObject {

    private int deletedObjectsCount;

    public DataReclaimed(Object source, int deletedObjectsCount) {
        super(source);
        this.deletedObjectsCount = deletedObjectsCount;
    }

    public int getDeletedObjectsCount() {
        return deletedObjectsCount;
    }
}
