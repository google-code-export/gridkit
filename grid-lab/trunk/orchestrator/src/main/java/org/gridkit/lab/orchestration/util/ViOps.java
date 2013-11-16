package org.gridkit.lab.orchestration.util;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.gridkit.lab.orchestration.Scope;
import org.gridkit.vicluster.ViNodeSet;

public class ViOps {
    public static boolean isScopeNode(Scope scope) {
        return scope.contains(getNodeName());
    }
    
    public static String getNodeName() {
        return System.getProperty("vinode.name");
    }
    
    public static <R> void execute(ViNodeSet cloud, String[] nodes, Callable<R> executor, ExecutionObserver<R> observer) {
        if (nodes.length == 0) {
            observer.completed();
        } else {
            ExecutionController<R> controller = new ExecutionController<R>(observer, nodes);
            RemoteExecutor<R> remoteExecutor = new RemoteExecutor<R>(executor, controller);
            cloud.nodes(nodes).submit(remoteExecutor);
        }
    }
    
    /**
     * It's guaranteed that all methods called in sequence in thread safe manner
     */
    public interface ExecutionObserver<R> {
        void completed(String node, R result);
        
        void failed(String node, Throwable error);
        
        void left(String node);
        
        void completed();
    }
    
    private interface RemoteObserver<R> extends ExecutionObserver<R>, Remote {}
    
    private static class ExecutionController<R> implements RemoteObserver<R> {
        private ExecutionObserver<R> delegate;
        private Set<String> nodes;
        
        public ExecutionController(ExecutionObserver<R> delegate, String[] nodes) {
            this.delegate = delegate;
            this.nodes = new HashSet<String>(Arrays.asList(nodes));
        }

        @Override
        public synchronized void completed(String node, R result) {
            delegate.completed(node, result);
            tryComplete(node);
        }

        @Override
        public synchronized void failed(String node, Throwable error) {
            delegate.failed(node, error);
            tryComplete(node);
        }

        @Override
        public void left(String node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void completed() {
            throw new UnsupportedOperationException();
        }
        
        private void tryComplete(String node) {
            nodes.remove(node);
            if (nodes.isEmpty()) {
                delegate.completed();
            }
        }
    }
    
    private static class RemoteExecutor<R> implements Runnable, Serializable {
        private static final long serialVersionUID = 3840093432228529708L;
        
        private Callable<R> delegate;
        private RemoteObserver<R> observer;
        
        public RemoteExecutor(Callable<R> delegate, RemoteObserver<R> observer) {
            this.delegate = delegate;
            this.observer = observer;
        }

        @Override
        public void run() {
            String node = getNodeName();
            
            R result;
            try {
                result = delegate.call();
            } catch (Exception e) {
                observer.failed(node, e);
                return;
            }
            
            observer.completed(node, result);
        }
    }
}
