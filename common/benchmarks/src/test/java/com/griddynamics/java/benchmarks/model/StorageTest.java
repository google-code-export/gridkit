package com.griddynamics.java.benchmarks.model;

import org.junit.Test;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import com.griddynamics.java.benchmarks.engine.TickCounter;

import java.util.concurrent.TimeUnit;

/**
 * User: akondratyev
 */
public class StorageTest {


    public void setUp() {

    }

    @Test
    public void selfRemovingData() throws Exception {
        Group gr = mock(Group.class);
        TickCounter ticks = mock(TickCounter.class);
        when(ticks.getTicks()).thenReturn(110);

        int lifeTime = 100;

        Storage storage = new Storage(gr, lifeTime, ticks);
        storage.push(1, new Entity());
        storage.push(2, new Entity());
        storage.push(3, new Entity());
        storage.push(5, new Entity());

        storage.push(102, new Entity());
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertEquals(1, storage.getBackMap().size());
    }


}
