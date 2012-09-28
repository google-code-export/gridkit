package org.gridkit.nimble.platform;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface RemoteAgent {
    String getId();
    
    Set<String> getLabels();
    
    InetAddress getInetAddress();
    
    int getPid();
    
    void shutdown(boolean hard);
    
    /**
     * @throws UnsupportedOperationException in a case of {@link LocalAgent}
     */
    <T> Future<T> invoke(Invocable<T> invocable);
    
    public interface Invocable<T> extends Serializable {
        T invoke(LocalAgent localAgent) throws Exception;
    }
    
    @SuppressWarnings("serial")
    public static class CallableInvocable<T> implements Invocable<T>, Serializable {
        private final Callable<T> delegate;

        public CallableInvocable(Callable<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T invoke(LocalAgent localAgent) throws Exception {
            return delegate.call();
        }
    }
}
