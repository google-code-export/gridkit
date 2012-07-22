package org.gridkit.nimble.platform;

import java.net.InetAddress;
import java.util.Set;

import com.google.common.util.concurrent.ListenableFuture;

public interface RemoteAgent extends TimeService {
    String getId();
    
    Set<String> getLabels();
    
    InetAddress getInetAddress();
    
    int getPid();
    
    void shutdown(boolean hard);
    
    /**
     * @throws UnsupportedOperationException in a case of {@link LocalAgent}
     */
    <T> ListenableFuture<T> invoke(Invocable<T> invocable);
    
    public interface Invocable<T> {
        T invoke(LocalAgent localAgent) throws Exception;
    }
}
