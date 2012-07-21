package org.gridkit.nimble.platform.local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gridkit.nimble.platform.LocalAgent;
import org.gridkit.nimble.util.SystemOps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class ThreadPoolAgent implements LocalAgent {
    private final String id;
    private final Set<String> labels;
    
    private final int pid;
    private final InetAddress inetAddress;
    
    private final ListeningExecutorService executor;
    
    private final ConcurrentMap<String, Object> attributes;
    
    public ThreadPoolAgent(ExecutorService executor, Set<String> labels) {
        this.id = UUID.randomUUID().toString();
        this.labels = labels;
        
        this.pid = SystemOps.getPid();
        
        try {
            this.inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        
        this.executor = MoreExecutors.listeningDecorator(executor);
        
        this.attributes = new ConcurrentHashMap<String, Object>();
    }
    
    public ThreadPoolAgent(ExecutorService executor) {
        this(executor, Collections.<String>emptySet());
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getLabels() {
        return labels;
    }

    @Override
    public InetAddress getInetAddress() {
        return inetAddress;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public <T> ListenableFuture<T> invoke(final Invocable<T> invocable) {
        return executor.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return invocable.invoke(ThreadPoolAgent.this);
            }
        });
    }

    @Override
    public Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public ConcurrentMap<String, Object> getAttributesMap() {
        return attributes;
    }
    
    @Override
    public void shutdown(boolean hard) {
        if (hard) {
            executor.shutdownNow();
        } else {
            executor.shutdown();
        }
    }
}
