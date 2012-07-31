package org.gridkit.concurrency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.gridkit.util.concurrent.BlockingBarrier;
import org.gridkit.util.concurrent.LatchBarrier;

public class SynchronizationDogRace {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        SynchronizationDogRace race = new SynchronizationDogRace();
        
        Sample[] samples;

        // warm up
        samples = race.testBarrier(new TimeRandomArrayWrite(1000), 4, 200000);
        samples = race.testBarrier(new Synchronized(new TimeRandomArrayWrite(1000)), 4, 200000);
        samples = race.testBarrier(new Locked(true, new TimeRandomArrayWrite(1000)), 4, 200000);
        samples = race.testBarrier(new Locked(false, new TimeRandomArrayWrite(1000)), 4, 200000);
        samples = race.testBarrier(new SysCall("."), 4, 200000);
        Thread.sleep(100);
        
        samples = race.testBarrier(new TimeRandomArrayWrite(1000000), 1, 2000000);
        race.displayStatistics(samples, -1);        

        samples = race.testBarrier(new TimeRandomArrayWrite(1000000), 4, 2000000);
        race.displayStatistics(samples, -1);        

        samples = race.testBarrier(new SysCall("."), 4, 2000000);
        race.displayStatistics(samples, -1);        

        dumpCsv("sys-call.csv", samples);

        samples = race.testBarrier(new Synchronized(new TimeRandomArrayWrite(1000000)), 16, 2000000);
        race.displayStatistics(samples, -1);        

        samples = race.testBarrier(new Locked(true, new TimeRandomArrayWrite(1000000)), 16, 2000000);
        race.displayStatistics(samples, -1);
        
        dumpCsv("fair-lock.csv", samples);

        samples = race.testBarrier(new Locked(false, new TimeRandomArrayWrite(1000000)), 16, 2000000);
        race.displayStatistics(samples, -1);        

        dumpCsv("unfair-lock.csv", samples);    
    }
    

    public void displayStatistics(Sample[] samples, int threadNo) {
        long start = Long.MAX_VALUE;
        long finish = 0;
        
        Mean mean = new Mean();
        StandardDeviation stdDev = new StandardDeviation();
        Skewness skewness = new Skewness();
        
        int n = 0;
        for(Sample sample: samples) {
            if (threadNo == -1 || threadNo == sample.threadId) {
                n++;
                if (sample.timestamp < start) {
                    start = sample.timestamp;
                }
                if (sample.timestamp + sample.duration > finish) {
                    finish = sample.timestamp + sample.duration;
                }
                mean.increment(sample.duration);
                stdDev.increment(sample.duration);
                skewness.increment(sample.duration);
            }
        }
        
        double throughPut = (double)n * (double)TimeUnit.SECONDS.toNanos(1) / (double)(finish - start);
        
        System.out.println(String.format("Mean(NS)    %.1f", mean.getResult()));
        System.out.println(String.format("StdDev(NS)  %.1f", stdDev.getResult()));
        System.out.println(String.format("Skew(NS)    %.1f", skewness.getResult()));
        System.out.println(String.format("Throughput  %.1f", throughPut));
        System.out.println(String.format("Total time  %.3f", (finish - start) / (double)TimeUnit.SECONDS.toNanos(1)));
        
    }
    
    public static void dumpCsv(String fileName, Sample[] samples) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        for(Sample sample: samples) {
            fw.append(String.valueOf(sample.timestamp));
            fw.append(',');
            fw.append(String.valueOf(sample.threadId));
            fw.append(',');
            fw.append(String.valueOf(sample.duration));
            fw.append('\n');
        }
        fw.close();
    }
    
    public Sample[] testBarrier(Runnable action, int threadNumber, int operations) throws InterruptedException, ExecutionException {
        
        System.out.println("Testing " + action + ", threads " + threadNumber + ", operations " + operations);
        
        LatchBarrier start = new LatchBarrier();
        int opsPerThread = operations / threadNumber;
        ExecutorService pool = Executors.newFixedThreadPool(threadNumber);

        List<WorkThread> workers = new ArrayList<WorkThread>();
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for(int i = 0; i != threadNumber; ++i) {
            WorkThread thread = new WorkThread(start, action, opsPerThread);
            futures.add(pool.submit(thread));
            workers.add(thread);
        }

        // TODO cyclic alike barrier
        Thread.sleep(10);
        
        long startTime = System.nanoTime();
        start.open();
        
        
        for(int i = 0; i != futures.size(); ++i) {
            futures.get(i).get();
        }

        Sample[] samples = new Sample[opsPerThread * threadNumber];
        
        int n = 0;
        for(int i = 0; i != futures.size(); ++i) {
            WorkThread worker = workers.get(i);
            for(int j = 0; j != opsPerThread; ++j) {
                long timestamp = worker.timestamps[j];
                long duration = worker.durations[j];
                
                Sample sample = new Sample(i, timestamp - startTime, duration);
                samples[n++] = sample;
            }
        }
        
        Arrays.sort(samples);
        
        pool.shutdown();
        
        return samples;
    }
    
    
    public class WorkThread implements Runnable {
        
        private BlockingBarrier startBarrier;
        private Runnable action;
        private long[] timestamps;
        private long[] durations;
        
        public WorkThread(BlockingBarrier start, Runnable action, int iterations) {
            this.startBarrier = start;
            this.action = action;
            timestamps = new long[iterations];
            durations = new long[iterations];
        }

        @Override
        public void run() {
            try {
                startBarrier.pass();
                for(int i = 0; i != timestamps.length; ++i) {
                    long enter = System.nanoTime();
                    action.run();
                    long leave = System.nanoTime();
                    timestamps[i] = enter;
                    durations[i] = leave - enter;
                }
            } catch (Exception e) {
                System.err.println("Unexpected thread termination: " + e.toString());
            }
        }
    }

    public static class Synchronized implements Runnable {
        
        private Runnable runnable;

        public Synchronized(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            synchronized (this) {
                runnable.run();
            }
        }
        
        public String toString() {
            return "Sync[" + runnable.toString() + "]";
        }
    }

    public static class Locked implements Runnable {
        
        private Lock lock;
        private Runnable runnable;
        private boolean fair;
        
        public Locked(boolean fair, Runnable runnable) {
            this.fair = fair;
            this.lock = new ReentrantLock(fair);
            this.runnable = runnable;            
        }
        
        @Override
        public void run() {
            lock.lock();
            try {
                runnable.run();
            }
            finally {
                lock.unlock();
            }
        }
        
        public String toString() {
            return (fair ? "LockFair" : "LockUnfair") + "[" + runnable.toString() + "]";
        }
    }
    
    public static class TimeRandomArrayWrite implements Runnable {

        int[] array;
        
        public TimeRandomArrayWrite(int size) {
            array = new int[size];
        }        
        
        @Override
        public void run() {
            long tid = Thread.currentThread().getId();
            long ns = System.nanoTime();
            
            Random rnd = new Random(tid ^ ns);
            int i = rnd.nextInt(array.length);
            array[i] = (int)ns;            
        }
        
        public String toString() {
            return "ArrayWrite[" + array.length + "]";
        }        
    }
    
    public static class SysCall implements Runnable {
        
        private File file;
        
        public SysCall(String file) {
            this.file = new File(file);
        }

        @Override
        public void run() {
            file.list();
        }
        
        public String toString() {
            return "SysCall";
        }
    }

}
