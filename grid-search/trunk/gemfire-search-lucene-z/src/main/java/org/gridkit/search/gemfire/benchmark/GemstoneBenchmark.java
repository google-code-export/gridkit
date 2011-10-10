package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.query.IndexType;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.distributed.DistributedSystem;
import org.gridkit.search.gemfire.benchmark.model.Commitment;
import org.gridkit.search.gemfire.benchmark.task.BenchmarkTask;
import org.gridkit.search.gemfire.benchmark.task.TaskExecutor;
import org.gridkit.search.gemfire.benchmark.task.GemstoneLineDistributionTask;
import org.gridkit.search.gemfire.benchmark.task.GemstonePositionKeyTask;

import java.util.concurrent.Callable;

import static org.gridkit.search.gemfire.benchmark.BenchmarkFactory.commitmentRegionName;

public class GemstoneBenchmark implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        Configuration config = new Configuration();

        BenchmarkFactory factory = new BenchmarkFactory(config);
        FtsData ftsData = new FtsData(config.ftsDataFolder);

        Cache cache = factory.createCache();

        Region<String, Commitment> commitmentRegion = factory.createRegion(
            cache, commitmentRegionName, false
        );

        QueryService qs = commitmentRegion.getRegionService().getQueryService();

        //qs.createIndex(
        //    "positionKey", IndexType.PRIMARY_KEY, "positionKey", commitmentRegion.getFullPath()
        //);
        
        //qs.createIndex(
        //    "responsibleDepartment", IndexType.FUNCTIONAL, "responsibleDepartment", commitmentRegion.getFullPath()
        //);

        qs.createIndex(
            "budgetLine", IndexType.FUNCTIONAL, "budgetLine", commitmentRegion.getFullPath()
        );


        System.out.println("Loading data ...");
        ftsData.fillRegion(commitmentRegion);

        DistributedSystem ds = cache.getDistributedSystem();

        GemstoneLineDistributionTask bt = new GemstoneLineDistributionTask(false, commitmentRegion);
        bt.setFtsData(ftsData);
        bt.setIterationsCount(5);
        TaskExecutor te = new TaskExecutor(bt, config.warmUpCount, ds);

        te.benchmark();

        return null;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");
        (new GemstoneBenchmark()).call();
    }
}