package org.gridkit.nimble;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.nimble.sensor.CpuDemon;
import org.gridkit.nimble.statistics.simple.SimplePrettyPrinter;
import org.gridkit.nimble.statistics.simple.SimplePrinter;
import org.gridkit.nimble.statistics.simple.SimpleStats;
import org.gridkit.nimble.statistics.simple.SimpleStatsProducer;
import org.junit.Ignore;

@Ignore
public class SigarTest {
    public static double sum = 0.0;
    
    public static void main(String[] args) throws Exception {
        SimpleStatsProducer r1 = new SimpleStatsProducer();
        SimpleStatsProducer r2 = new SimpleStatsProducer();
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        CpuDemon cpuRep = new CpuDemon("LOCAL", new CpuDemon.CurPidCpuReporter(), r1, 100, 200);
        
        CpuDemon pcpuRep = new CpuDemon("CHROME", new CpuDemon.PtqlCpuReporter("Exe.Name.ct=java"),  r2, 100, 200);
        
        executor.submit(new SinCalc());
        executor.submit(new SinCalc());
        executor.submit(cpuRep);
        executor.submit(pcpuRep);
        
        Thread.sleep(5000);
        
        executor.shutdownNow();
        
        SimplePrinter printer = new SimplePrettyPrinter();
        
        printer.printValues(System.err, SimpleStats.combine(r1.produce(), r2.produce()));
        
        printer.printValues(System.err, r1.produce());
        
        printer.printValues(System.err, r2.produce());
        
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
