package org.gridkit.nimble.sensor;

public interface Sensor<M> {
    M measure();
    
    long getSleepTimeMs();
    
    public static interface Reporter<M> {
        void report(M m1, M m2, long timeNs);
    }
}
