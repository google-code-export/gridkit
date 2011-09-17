package org.gridkit.util.classloader;

import java.util.concurrent.Callable;

public class IsolatedCallable<T> implements Callable<T> {
    private Callable<T> delegate;

    public IsolatedCallable(Callable<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T call() throws Exception {
        ClassLoader contextClassLoader = delegate.getClass().getClassLoader();
        ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return delegate.call();
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new RuntimeException(t);
        }
        finally {
            Thread.currentThread().setContextClassLoader(initialClassLoader);
        }
    }
}
