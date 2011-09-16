package org.gridkit.gemfire.search.demo;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.compass.core.spi.InternalCompass;
import org.gridkit.search.gemfire.SearchServerConfig;
import org.gridkit.search.gemfire.SearchServerFactory;

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

        createServerCache(SearchServerFactory.searchServerRole);

        PoolFactory poolFactory = PoolManager.createFactory();
        poolFactory.addLocator(locatorHost, locatorPort);
        poolFactory.setSubscriptionEnabled(true);
        Pool pool = poolFactory.create("search-pool");

        SearchServerFactory searchServerFactory = new SearchServerFactory(searchServerConfig, compass);

        FunctionService.registerFunction(searchServerFactory.getDiscoveryFunction());
        FunctionService.registerFunction(searchServerFactory.getSearchFunction());

        searchServerFactory.createRegionIndex("/" + authorRegionName, pool.getQueryService(), Executors.newSingleThreadExecutor());

        searchServerLatch.countDown();

        return null;
    }
}
