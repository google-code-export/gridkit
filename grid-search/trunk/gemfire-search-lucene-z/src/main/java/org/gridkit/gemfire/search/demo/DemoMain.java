package org.gridkit.gemfire.search.demo;

import org.gridkit.util.classloader.ImdgClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class DemoMain {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        List<String> isolateList = Arrays.asList("org.gridkit.", "com.gemstone.", "org.compass.");

        CountDownLatch locatorLatch = new CountDownLatch(1);
        CountDownLatch storeLatch = new CountDownLatch(1);
        CountDownLatch searchServerLatch = new CountDownLatch(1);

        Callable<Void> locator = getIsolatedCallable(LocatorNode.class.getName(), isolateList, locatorLatch);
        Callable<Void> store = getIsolatedCallable(StoreNode.class.getName(), isolateList, locatorLatch, storeLatch);
        Callable<Void> searchServer = getIsolatedCallable(SearchServerNode.class.getName(), isolateList, storeLatch, searchServerLatch);
        Callable<Void> searchClient = getIsolatedCallable(SearchClientNode.class.getName(), isolateList, searchServerLatch);

        threadPool.submit(new IsolatedCallable<Void>(locator));
        threadPool.submit(new IsolatedCallable<Void>(store));

        threadPool.submit(new IsolatedCallable<Void>(searchServer));
        threadPool.submit(new IsolatedCallable<Void>(searchClient));

        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        System.exit(0);
    }

    private static Callable<Void> getIsolatedCallable(String className, List<String> isolatePrefixList, Object... args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<Callable<Void>> clazz = getIsolatedClass(className, isolatePrefixList);

        List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

        for(Object arg : args)
            parameterTypes.add(arg.getClass());

        return clazz.getConstructor(parameterTypes.toArray(new Class<?>[args.length])).newInstance(args);
    }

    private static <T> Class<T> getIsolatedClass(String className,  List<String> isolatePrefixList) throws ClassNotFoundException {
        URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();

        ImdgClassLoader imdgClassLoader = new ImdgClassLoader(
            systemClassLoader.getURLs(), isolatePrefixList, Collections.<String>emptyList()
        );

        @SuppressWarnings("unchecked")
        Class<T> isolatedClazz = (Class<T>)Class.forName(className, true, imdgClassLoader);

        return isolatedClazz;
    }
}
