package com.griddynamics.java.benchmarks.engine;

import org.junit.Assert;
import org.junit.Test;
import com.griddynamics.java.benchmarks.model.event.TickListener;
import com.griddynamics.java.benchmarks.model.event.TickEvent;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: akondratyev
 * Date: Dec 20, 2010
 * Time: 5:59:31 PM
 */
public class TestTickCounter {

    @Test
    public void incrementTick() {
        TickCounter counter = new TickCounter();
        long previousTick = counter.getTicks();
        counter.incrementTick();
        long currentTick = counter.getTicks();
        Assert.assertEquals(currentTick - previousTick, 1l);
    }

}
