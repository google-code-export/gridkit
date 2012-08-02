package org.gridkit.nimble.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.gridkit.nimble.platform.FuturePoller;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueuedFuturePoller implements FuturePoller {
    private final BlockingQueue<Pair<Future, SettableFuture>> futures;
    
    private final ExecutorService executor;

    public QueuedFuturePoller(int nThreads) {
        this.futures = new LinkedBlockingQueue<Pair<Future,SettableFuture>>();
        this.executor = Executors.newFixedThreadPool(nThreads);
        
        while (nThreads-- > 0) {
            this.executor.submit(new PollWorker());
        }
    }

    @Override
    public <T> ListenableFuture<T> poll(Future<T> future) {
        SettableFuture result = SettableFuture.create();
        
        futures.add(Pair.newPair((Future)future, result));
        
        return result;
    }
    
    private class PollWorker implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    Pair<Future,SettableFuture> pair = futures.take();
                    
                    Future future = pair.getA();
                    SettableFuture result = pair.getB();
                    
                    if (future.isDone()) {
                        if (future.isCancelled()) {
                            result.cancel(true);
                        } else {
                            try {
                                result.set(future.get());
                            } catch (ExecutionException e) {
                                result.setException(e.getCause());
                            }
                        }
                    } else {
                        futures.put(pair);
                    }
                }
            }
            catch (InterruptedException onShutdown) {}
        }
    }



    @Override
    public void shutdown() {
        executor.shutdownNow();
    }
}
