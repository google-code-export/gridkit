package org.gridkit.search.gemfire.benchmark.task;

import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.google.common.base.Stopwatch;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gridkit.search.gemfire.benchmark.GcFunction;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
    private int warmUpCount;
    private BenchmarkTask task;
    private DistributedSystem ds;

    public TaskExecutor(BenchmarkTask task, int warmUpCount, DistributedSystem ds) {
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
        int testNumber = 0;
        
        Stopwatch overallSw = new Stopwatch();
        DescriptiveStatistics overallSt = new DescriptiveStatistics();

        task.reset();

        FunctionService.onMembers(ds).execute(GcFunction.Instance).getResult();

        boolean doNext = false;
        do {
            testNumber += 1;
            
            if (testNumber % 500 == 1)
                System.out.println("Running test number " + testNumber);
            
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

        System.out.println("Name,N,Mean,Std,k.1,k.5,k.9,Min,Max");
        
        for (Map.Entry<String, DescriptiveStatistics> result : results.entrySet())
            System.out.println(String.format(
                "%s,%s,%s,%s,%s,%s,%s,%s,%s",
                result.getKey(),
                String.valueOf(result.getValue().getN()),
                df.format(result.getValue().getMean()),
                df.format(result.getValue().getStandardDeviation()),
                df.format(result.getValue().getPercentile(10)),
                df.format(result.getValue().getPercentile(50)),
                df.format(result.getValue().getPercentile(90)),
                df.format(result.getValue().getMin()),
                df.format(result.getValue().getMax())
            ));
    }
}
