package org.gridkit.search.gemfire.benchmark;

import static org.gridkit.search.gemfire.benchmark.BenchmarkFactory.*;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedSystem;
import org.gridkit.search.gemfire.GemfireIndexSearcher;
import org.gridkit.search.gemfire.IndexDiscoveryFunction;
import org.gridkit.search.gemfire.IndexSearchFunction;
import org.gridkit.search.gemfire.benchmark.model.Commitment;
import org.gridkit.search.gemfire.benchmark.task.BenchmarkTask;
import org.gridkit.search.gemfire.benchmark.task.GemfireTaskExecutor;
import org.gridkit.search.gemfire.benchmark.task.LucenePositionKeyTask;

import java.util.concurrent.Callable;

public class LuceneBenchmark implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        Configuration config = new Configuration();

        BenchmarkFactory factory = new BenchmarkFactory(config);
        FtsData ftsData = new FtsData(config.ftsDataFolder);

        Cache cache = factory.createCache();

        Region<String, Commitment> commitmentRegion = factory.createRegion(
            cache, commitmentRegionName, false
        );

        ftsData.fillRegion(commitmentRegion);

        Thread.sleep(10000);

        FunctionService.registerFunction(IndexDiscoveryFunction.getIndexDiscoveryFunctionStub());
        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub());

        DistributedSystem ds = cache.getDistributedSystem();
        GemfireIndexSearcher searcher = new GemfireIndexSearcher(ds);

        BenchmarkTask bt = new LucenePositionKeyTask(searcher, commitmentRegion);
        bt.setFtsData(ftsData);
        GemfireTaskExecutor te = new GemfireTaskExecutor(bt, 5, ds);
        te.benchmark();

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new LuceneBenchmark()).call();
    }
}
