package com.griddynamics.java.benchmarks.model.event;

import java.util.EventObject;

/**
 * User: akondratyev
 * Date: Dec 17, 2010
 * Time: 2:12:03 PM
 */
public class TickEvent extends EventObject {

    private long tick;

    public TickEvent(Object source, long tick) {
        super(source);
        this.tick = tick;
    }

    public long getCurrentTick() {
        return tick;
    }

    @Override
    public String toString() {
        return "current tick is " + tick;
    }
}
