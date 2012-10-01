package org.gridkit.nimble.sensor;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class SensorDemon<M> implements Callable<Void>, Serializable {
    private static final Logger log = LoggerFactory.getLogger(SensorDemon.class);
    
    private Sensor<M> sensor;
    private Sensor.Reporter<M> reporter;
    private boolean ignoreFailures;

    public SensorDemon(Sensor<M> sensor, Sensor.Reporter<M> reporter) {
        this.sensor = sensor;
        this.reporter = reporter;
        this.ignoreFailures = true;
    }

    public Void callInternal() throws Exception {                
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
    
    @Override
    public Void call() throws Exception {
        while (true) {
            try {
                callInternal();
            } catch (Throwable t) {
                if (!(t instanceof InterruptedException)) {
                    log.error("Throwable while executing SensorDemon", t);
                    if (!ignoreFailures) {
                        log.error("SensorDemon will be terminated");
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }
}
