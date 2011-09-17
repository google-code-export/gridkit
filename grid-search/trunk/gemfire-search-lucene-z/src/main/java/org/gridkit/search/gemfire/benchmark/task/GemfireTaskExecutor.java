package org.gridkit.search.gemfire.benchmark.task;

import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.google.common.base.Stopwatch;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gridkit.search.gemfire.benchmark.GcFunction;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GemfireTaskExecutor {
    private int warmUpCount;
    private BenchmarkTask task;
    private DistributedSystem ds;

    public GemfireTaskExecutor(BenchmarkTask task, int warmUpCount, DistributedSystem ds) {
        this.warmUpCount = warmUpCount;
        this.task = task;
        this.ds = ds;
    }

    public void benchmark() throws Exception {
        for (int i = 0; i < warmUpCount; ++i) {
            System.out.println("------------------ Starting warm up number " + (i + 1) + " ------------------");
            printResult(execute());
        }

        System.out.println("------------------ Starting final benchmark ------------------");
        printResult(execute());
    }

    private Map<String, DescriptiveStatistics> execute() throws Exception{
        Stopwatch overallSw = new Stopwatch();
        DescriptiveStatistics overallSt = new DescriptiveStatistics();

        task.reset();

        FunctionService.onMembers(ds).execute(GcFunction.Instance).getResult();

        boolean doNext = false;
        do {
            overallSw.start();
            doNext = task.execute();
            overallSw.stop();

            overallSt.addValue(overallSw.elapsedTime(TimeUnit.MICROSECONDS));
            overallSw.reset();

            task.record();
        } while (doNext);

        Map<String, DescriptiveStatistics> result = task.getStatistics();
        result.put("overall", overallSt);

        return result;
    }

    private static void printResult(Map<String, DescriptiveStatistics> results) {
        DecimalFormat df = new DecimalFormat("#.##");

        for (Map.Entry<String, DescriptiveStatistics> result : results.entrySet())
            System.out.println(String.format(
                "%s [mean = %s | std = %s | k.1 = %s | k.5 = %s | k.9 = %s | min = %s | max = %s]",
                result.getKey(),
                df.format(result.getValue().getMean()),
                df.format(result.getValue().getStandardDeviation()),
                df.format(result.getValue().getPercentile(0.1)),
                df.format(result.getValue().getPercentile(0.5)),
                df.format(result.getValue().getPercentile(0.9)),
                df.format(result.getValue().getMin()),
                df.format(result.getValue().getMax())
            ));
    }
}
