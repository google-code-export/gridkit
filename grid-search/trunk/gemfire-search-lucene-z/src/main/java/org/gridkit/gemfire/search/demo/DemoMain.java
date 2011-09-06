package org.gridkit.gemfire.search.demo;

import org.gridkit.util.classloader.ImdgClassLoader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoMain {
    public static void main(String[] args) throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        List<String> isolateList = Arrays.asList("org.gridkit.", "com.gemstone.", "org.compass.");

        Callable<Void> searchNode = ImdgClassLoader.getIsolatedClass(SearchNode.class.getName(), isolateList);
        Callable<Void> cacheNode = ImdgClassLoader.getIsolatedClass(CacheNode.class.getName(), isolateList);

        threadPool.submit(new IsolatedCallable<Void>(searchNode));
        threadPool.submit(new IsolatedCallable<Void>(cacheNode));

        threadPool.shutdown();
    }
}
