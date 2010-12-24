package com.griddynamics.java.benchmarks.model.event;

/**
 * User: akondratyev
 * Date: Dec 17, 2010
 * Time: 2:16:12 PM
 */
public interface TickListener {

   public void tickHappened(TickEvent tick);

   public long getFactor();
}
