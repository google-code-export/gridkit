package org.gridkit.nimble.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

public class FutureOps {
    public static <V> void addListener(ListenableFuture<V> future, FutureListener<? super V> listener, Executor executor) {
        ValidOps.notNull(future, "future");
        ValidOps.notNull(listener, "listener");
        ValidOps.notNull(executor, "executor");

        future.addListener(new RunnableListener<V>(future, listener), executor);
    }
    
    private static class RunnableListener<V> implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(RunnableListener.class);
        
        private final ListenableFuture<V> future;
        private FutureListener<? super V> listener;
        
        public RunnableListener(ListenableFuture<V> future, FutureListener<? super V> listener) {
            this.future = future;
            this.listener = listener;
        }

        @Override
        public void run() {
            boolean isSuccess = false;
            V result = null;

            try {
                result = Uninterruptibles.getUninterruptibly(future);
                isSuccess = true;
            } catch (CancellationException e) {
                onCancel();
            } catch (ExecutionException e) {
                onFailure(e.getCause(), false, false);
            }
            
            if (isSuccess) {
                onSuccess(result);
            }
        }
        
        private void onSuccess(V result) {
            try {
                listener.onSuccess(result);
            } catch (Throwable t) {
                log.error("Throwable while executing onSuccess", t);
                onFailure(t, true, false);
            }
        }

        private void onCancel() {
            try {
                listener.onCancel();
            } catch (Throwable t) {
                log.error("Throwable while executing onCancel", t);
                onFailure(t, false, true);
            }
        }
        
        private void onFailure(Throwable t, boolean afterSuccess, boolean afterCancel) {
            try {
                listener.onFailure(t, afterSuccess, afterCancel);
            } catch (Throwable tt) {
                log.error("Throwable while executing onFailure", tt);
                throw new RuntimeException(tt);
            }
        }
    }
}
