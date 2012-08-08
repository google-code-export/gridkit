package org.gridkit.nimble.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final String name;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public NamedThreadFactory(String name) {
        this.name = name;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        int number = counter.getAndIncrement();
        return new Thread(r, name + "-" + number);
    }
}
