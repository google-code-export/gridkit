package org.gridkit.gemfire.search.demo;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.server.CacheServer;
import org.gridkit.gemfire.search.lucene.IndexDiscoveryFunction;
import org.gridkit.gemfire.search.lucene.IndexSearchFunction;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class StoreNode implements Callable<Void> {
    private CountDownLatch locatorLatch;
    private CountDownLatch storeLatch;

    public StoreNode(CountDownLatch locatorLatch, CountDownLatch storeLatch) {
        this.locatorLatch = locatorLatch;
        this.storeLatch = storeLatch;
    }

    @Override
    public Void call() throws Exception {
        locatorLatch.await();

        Cache cache = createServerCache();

        CacheServer cacheServer = cache.addCacheServer();
        cacheServer.start();

        createServerRegion(cache, DemoFactory.authorRegionName, true);

        FunctionService.registerFunction(IndexDiscoveryFunction.getIndexDiscoveryFunctionStub());
        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub());

        storeLatch.countDown();

        return null;
    }
}
