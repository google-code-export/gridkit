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

    @Override
    public Void call() throws Exception {
        while (!Thread.interrupted()) {
            try {
                M m = sensor.measure();
                reporter.report(m);
            } catch (InterruptedException e) {
                return null;
            } catch (Throwable t) {
                log.error("Throwable while executing SensorDemon", t);
                
                if (!ignoreFailures) {
                    log.error("SensorDemon will be terminated");
                    return null;
                }
            }
        }
        
        return null;
    }
}
