package org.gridkit.gemfire.search.demo;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.compass.core.spi.InternalCompass;
import org.gridkit.gemfire.search.lucene.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class SearchServerNode implements Callable<Void> {
    private CountDownLatch storeLatch;
    private CountDownLatch searchServerLatch;

    public SearchServerNode(CountDownLatch storeLatch, CountDownLatch searchServerLatch) {
        this.storeLatch = storeLatch;
        this.searchServerLatch = searchServerLatch;
    }

    @Override
    public Void call() throws Exception {
        storeLatch.await();

        SearchServerConfig searchServerConfig = new SearchServerConfig();

        InternalCompass compass = createCompass();

        ClientCache cache = createClientCache();
        Region authorRegion = createClientRegion(cache, authorRegionName);

        SearchServerFactory searchServerFactory = new SearchServerFactory(searchServerConfig, compass);

        FunctionService.registerFunction(searchServerFactory.getDiscoveryFunction());
        FunctionService.registerFunction(searchServerFactory.getSearchFunction());

        searchServerFactory.createRegionIndex(authorRegion, Executors.newSingleThreadExecutor());

        searchServerLatch.countDown();

        return null;
    }
}
