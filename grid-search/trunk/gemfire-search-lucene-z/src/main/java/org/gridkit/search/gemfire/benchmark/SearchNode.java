package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.compass.core.spi.InternalCompass;
import org.gridkit.search.gemfire.SearchServerConfig;
import org.gridkit.search.gemfire.SearchServerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.gridkit.search.gemfire.benchmark.BenchmarkFactory.*;

public class SearchNode implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        Configuration config = new Configuration();
        BenchmarkFactory factory = new BenchmarkFactory(config);

        SearchServerConfig searchServerConfig = new SearchServerConfig();
        searchServerConfig.setChangesBeforeCommit(10000);

        InternalCompass compass = factory.createCompass();

        factory.createCache(SearchServerFactory.searchServerRole);

        PoolFactory poolFactory = PoolManager.createFactory();
        poolFactory.addLocator(config.locatorHost, config.locatorPort);
        poolFactory.setSubscriptionEnabled(true);
        Pool pool = poolFactory.create("search-pool");

        SearchServerFactory searchServerFactory = new SearchServerFactory(
            searchServerConfig, compass
        );

        FunctionService.registerFunction(searchServerFactory.getDiscoveryFunction());
        FunctionService.registerFunction(searchServerFactory.getSearchFunction());

        searchServerFactory.createRegionIndex(
            "/" + commitmentRegionName, pool.getQueryService(), Executors.newSingleThreadExecutor()
        );

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new SearchNode()).call();
        Thread.sleep(Long.MAX_VALUE);
    }
}