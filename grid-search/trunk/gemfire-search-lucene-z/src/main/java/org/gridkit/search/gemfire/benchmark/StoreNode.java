package org.gridkit.search.gemfire.benchmark;

import static org.gridkit.search.gemfire.benchmark.BenchmarkFactory.*;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.server.CacheServer;

import java.util.concurrent.Callable;

public class StoreNode implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        Configuration config = new Configuration();
        BenchmarkFactory factory = new BenchmarkFactory(config);

        Cache cache = factory.createCache();

        CacheServer cacheServer = cache.addCacheServer();
        cacheServer.start();

        factory.createRegion(cache, commitmentRegionName, true);

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new StoreNode()).call();
        Thread.sleep(Long.MAX_VALUE);
    }
}
