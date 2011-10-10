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
import org.gridkit.search.gemfire.benchmark.task.TaskExecutor;
import org.gridkit.search.gemfire.benchmark.task.LuceneLineDistributionTask;
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

        System.out.println("Loading data ...");
        ftsData.fillRegion(commitmentRegion);

        Thread.sleep(1000);

        FunctionService.registerFunction(IndexDiscoveryFunction.getIndexDiscoveryFunctionStub());
        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub());

        DistributedSystem ds = cache.getDistributedSystem();
        GemfireIndexSearcher searcher = new GemfireIndexSearcher(ds);

        LuceneLineDistributionTask bt = new LuceneLineDistributionTask(true, searcher, commitmentRegion);
        bt.setFtsData(ftsData);
        bt.setIterationsCount(5);
        TaskExecutor te = new TaskExecutor(bt, config.warmUpCount, ds);
        te.benchmark();

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new LuceneBenchmark()).call();
    }
}
