package com.griddynamics.java.benchmarks.model.event;

/**
 * uset to restore objectsCount for Group
 * User: akondratyev
 */
public interface DataReclaimingListener {

    public void dataReclaiming(DataReclaimed event);
}
