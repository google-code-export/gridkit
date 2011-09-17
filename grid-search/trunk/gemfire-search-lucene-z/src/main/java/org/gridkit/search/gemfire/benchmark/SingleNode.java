package org.gridkit.search.gemfire.benchmark;

import org.gridkit.util.classloader.ImdgClassLoader;
import org.gridkit.util.classloader.IsolatedCallable;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleNode {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        List<String> isolateList = Arrays.asList("org.gridkit.", "com.gemstone.", "org.compass.");

        Callable<Void> locator = getIsolatedCallable(LocatorNode.class.getName(), isolateList);
        Callable<Void> store = getIsolatedCallable(StoreNode.class.getName(), isolateList);

        Callable<Void> search = getIsolatedCallable(SearchNode.class.getName(), isolateList);
        Callable<Void> lucene = getIsolatedCallable(LuceneBenchmark.class.getName(), isolateList);

        Callable<Void> gemstone = getIsolatedCallable(GemstoneBenchmark.class.getName(), isolateList);

        threadPool.submit(new IsolatedCallable<Void>(locator)).get();
        threadPool.submit(new IsolatedCallable<Void>(store)).get();

        //threadPool.submit(new IsolatedCallable<Void>(search)).get();
        //threadPool.submit(new IsolatedCallable<Void>(lucene)).get();

        threadPool.submit(new IsolatedCallable<Void>(gemstone)).get();

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
