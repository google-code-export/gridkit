package org.gridkit.nimble;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.nimble.sensor.IntervalMeasure;
import org.gridkit.nimble.sensor.PidProvider;
import org.gridkit.nimble.sensor.ProcCpuReporter;
import org.gridkit.nimble.sensor.ProcCpuSensor;
import org.gridkit.nimble.sensor.SensorDemon;
import org.gridkit.nimble.statistics.StatsReporter;
import org.gridkit.nimble.statistics.simple.AggregatingSimpleStatsReporter;
import org.gridkit.nimble.statistics.simple.QueuedSimpleStatsAggregator;
import org.gridkit.nimble.statistics.simple.SimplePrettyPrinter;
import org.gridkit.nimble.statistics.simple.SimplePrinter;
import org.gridkit.nimble.statistics.simple.SimpleStatsAggregator;
import org.hyperic.sigar.ProcCpu;
import org.junit.Ignore;

@Ignore
public class SigarTest {
    public static double sum = 0.0;
    
    public static void main(String[] args) throws Exception {
        SimpleStatsAggregator aggr = new QueuedSimpleStatsAggregator();
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        StatsReporter cpuRep = new AggregatingSimpleStatsReporter(aggr, 2);
        
        SensorDemon<?> cpuDemon = new SensorDemon<List<IntervalMeasure<ProcCpu>>>(
           new ProcCpuSensor(new PidProvider.CurPidProvider()), new ProcCpuReporter("LOCAL", cpuRep)
        );
        
        StatsReporter pcpuRep = new AggregatingSimpleStatsReporter(aggr, 2);

        SensorDemon<?> pcpuDemon = new SensorDemon<List<IntervalMeasure<ProcCpu>>>(
            new ProcCpuSensor(new PidProvider.PtqlPidProvider("Exe.Name.ct=java")), new ProcCpuReporter("JAVA", pcpuRep)
        );
        
        executor.submit(new SinCalc());
        executor.submit(new SinCalc());
        executor.submit(cpuDemon);
        executor.submit(pcpuDemon);
        
        Thread.sleep(10000);
        
        executor.shutdownNow();
        
        SimplePrinter printer = new SimplePrettyPrinter();
        
        printer.printValues(System.err, aggr.calculate());
    }
    
    private static class SinCalc implements Callable<Void> {
        @Override
        @SuppressWarnings("static-access")
        public Void call() throws Exception {
            while (!Thread.currentThread().interrupted()) {
                sum += Math.sin(new Random().nextInt());
            }
            return null;
        }
    }
}
