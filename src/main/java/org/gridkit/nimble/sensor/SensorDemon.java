package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.concurrent.Callable;

@SuppressWarnings("serial")
public class SensorDemon<M> implements Callable<Void>, Serializable {
    private Sensor<M> sensor;
    private Sensor.Reporter<M> reporter;

    public SensorDemon(Sensor<M> sensor, Sensor.Reporter<M> reporter) {
        this.sensor = sensor;
        this.reporter = reporter;
    }

    @Override
    public Void call() throws Exception {
        long ts11 = System.nanoTime();
        M m1 = sensor.measure();
        long ts12 = System.nanoTime();
        
        while (!Thread.interrupted()) {
            Thread.sleep(sensor.getSleepTimeMs());
            
            long ts21 = System.nanoTime();
            M m2 = sensor.measure();
            long ts22 = System.nanoTime();

            long timeNs = (ts22 + ts21)/2 - (ts11 + ts12)/2;
            
            reporter.report(m1, m2, timeNs);

            m1 = m2;
            ts11 = ts21;
            ts12 = ts22;
        }
        
        return null;
    }

}
