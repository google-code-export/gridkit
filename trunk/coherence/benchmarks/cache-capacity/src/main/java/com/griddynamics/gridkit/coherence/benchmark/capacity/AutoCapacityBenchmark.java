package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.SimpleCohCloud;
import org.gridkit.lab.jvm.attach.AttachManager;
import org.gridkit.lab.jvm.attach.HeapHisto;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.junit.Test;

import sample.SimpleDomainObjGenerator;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.ObjectGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class AutoCapacityBenchmark {

	private String scheme = "simple-distributed-scheme";
	private String storageVmOptions = "|-Xmx800m|-Xms800m";
	private int objectCount = 1000000;
	
	
	@Test
	public void test_raw_data() throws Exception {
		final String cacheName = "objects";
		
	    SimpleCohCloud cloud = new SimpleCohCloud();
	    cloud.all().outOfProcess(true);

	    cloud.node("client").localStorage(false);
	    cloud.node("storage").localStorage(true);
	    JvmProps.at(cloud.node("storage")).addJvmArg(storageVmOptions);
	    
        cloud.all().setProp("tangosol.pof.enabled", "true");
        cloud.all().setProp("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
        cloud.all().setProp("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");

        cloud.all().setProp("benchmark-default-scheme", scheme);
        
        cloud.all().getCache(cacheName);
        
        int storagePid = cloud.node("storage").exec(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                name = name.substring(0, name.indexOf("@"));
                return Integer.parseInt(name);
            }
        });
        
        HeapHisto initial = histo(storagePid, true);
        System.out.println("Initial heap size: " + (initial.totalBytes() >> 20) + "MiB");
        
        Loader loader = new Loader();
        loader.cacheName = cacheName;
        loader.rangeStart = 1000000;        
        loader.rangeFinish = 1000000 + objectCount;
        
        
        System.out.println("Cold loading");
        cloud.node("client").exec(loader);
        HeapHisto loaded1 = histo(storagePid, true);
        
        System.out.println("Storage heap usage delta: " + ((loaded1.totalBytes() - initial.totalBytes()) >> 20) + "MiB");
        
        cloud.node("client").getCache(cacheName).clear();
        cloud.node("client").getCache(cacheName).size();

        HeapHisto empty = histo(storagePid, true);
        System.out.println("Empty heap size: " + (empty.totalBytes() >> 20) + "MiB");

        System.out.println("Warm loading");
        cloud.node("client").exec(loader);
        HeapHisto loaded2 = histo(storagePid, true);
		
        System.out.println("Storage heap usage delta: " + ((loaded2.totalBytes() - empty.totalBytes()) >> 20) + "MiB");
        
        System.out.println("Storage heap full vs initial summary:");
        System.out.println(HeapHisto.subtract(loaded1, initial).print(30));

        System.out.println("Storage heap full vs empty summary:");
        System.out.println(HeapHisto.subtract(loaded2, empty).print(30));

        System.out.println("Storage full heap summary:");
        System.out.println(loaded2.print(30));

        System.out.println("Storage heap initial vs empty summary:");
        System.out.println(HeapHisto.subtract(empty, initial).print(30));
        
	}
	
	private static HeapHisto histo(int pid, boolean live) throws Exception {
        String[] pall = { "-all" };
        String[] plive = { "-live" };
        List<String> hh = AttachManager.getHeapHisto(pid, live ? plive : pall, 30000);
        return HeapHisto.parse(hh);
	}

	@SuppressWarnings("serial")
    private static class Loader implements Runnable, Serializable {
		
		String cacheName;
		long rangeStart;
		long rangeFinish;
		
        @Override
        public void run() {
            final NamedCache cache = CacheFactory.getCache(cacheName);
            final ObjectGenerator<?, ?> generator = new SimpleDomainObjGenerator();
            
            println("Loading " + (rangeFinish - rangeStart) + " objects ...");
            for(long i = rangeStart;  i < rangeFinish; i += 100) {
                if (i % 100000 == 0) {
                    println("Done " + (i - rangeStart));
                }
                long j = Math.min(rangeFinish, i + 100);
                cache.putAll(generator.generate(i, j));
            }           
            
        }
	}
	
    static void println() {
        System.out.println();
    }
    
    static void println(String text) {
        System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
    }	
}
