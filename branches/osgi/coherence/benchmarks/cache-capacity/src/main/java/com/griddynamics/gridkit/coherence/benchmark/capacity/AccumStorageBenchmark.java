/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package com.griddynamics.gridkit.coherence.benchmark.capacity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.accum.Accumulator;
import com.griddynamics.gridkit.coherence.benchmark.capacity.objects.accum.ObjectGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;

public class AccumStorageBenchmark {
	
	private final static ExecutorService EXECUTORS = Executors.newCachedThreadPool();
	
//	private final long sideStart = 1 * 1000 * 1000 * 1000;
//	private final long sideFinish = ;
	
	private static BlockingQueue<Map<Long, Accumulator>> WRITE_QUEUE = new SynchronousQueue<Map<Long,Accumulator>>(true);

	private static BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
	
	public static void main(String[] args) {
		try {
			final NamedCache cache = CacheFactory.getCache("accumulators");
			
//			Accumulator accum = new Accumulator();
//			Attribute attr = new Attribute();
//			attr.addValue("123");
//			attr.addValue("456");
//			accum.addAttribute(attr);
//			Threshold threshold = new Threshold();
//			threshold.setValue(10);
//			accum.addThreshold(threshold);
//			
//			Binary bin = ExternalizableHelper.toBinary(accum);
//			Accumulator accum2 = (Accumulator) ExternalizableHelper.fromBinary(bin);
			
			if (!Boolean.getBoolean("test.disableIndex")) {
				cache.addIndex(new ReflectionExtractor("getSide"), false, null);
			}

			if (!Boolean.getBoolean("test.disableGenerate")) {
			
				int sideRange = Integer.getInteger("test.sideRange", 10000);
				
				ObjectGenerator generator = new ObjectGenerator();
				long start = 1 * 1000 * 1000 * 1000;
				long finish = start + sideRange;
	//			long finish = start + 200000;
				long next = start;
				System.out.println("Start side: " + start);
				System.out.println("Finish side: " + finish);
				
				long accumsCount = 0;
				
				int writeThreadCount = Integer.getInteger("test.writeThreads", 2);			
				
				for(int i = 0; i != writeThreadCount; ++i) {
					EXECUTORS.execute(new Runnable() {
					
						@Override
						public void run() {
							try {
								while(true) {
									Map<Long, Accumulator> map = WRITE_QUEUE.take();
									if (map.isEmpty()) {
										break;
									}
									cache.putAll(map);
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
					});
				}
				
	
				while(next < finish) {
					
					Map<Long, Accumulator> accums = generator.generate(next, next + 1000);
					next += 1000;
					
					WRITE_QUEUE.put(accums);
					accumsCount += accums.size();
					System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] %2$d", new Date(), accumsCount));
				}
				
				for(int i = 0; i != writeThreadCount; ++i) {
					WRITE_QUEUE.put(new HashMap<Long, Accumulator>());
				}
				
				System.gc();
				System.out.println("Done.");
				System.out.println("Memory used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20) + "M");
				System.out.println("AccumsCount: " + generator.getAccumCount());
				System.out.println("AttribCount: " + generator.getAttribCount());
				System.out.println("ThresholdCount: " + generator.getThresholdCount());
				System.out.println("RuleCount: " + generator.getRuleCount());
				
//				DEFLATE = DEFLATE.copy();
//				INFLATE = INFLATE.copy();
//				BYTE_SIZE = BYTE_SIZE.copy();
//				ITEM_SIZE = BYTE_SIZE.copy();
//				RATIO = RATIO.copy();
//	
//				int simThreadsCount = Integer.getInteger("test.operationThreads", 6);
//				
//				if (!Boolean.getBoolean("test.disablePerformaceTest")) {
//					
//					if (Boolean.getBoolean("test.interactive")) {
//						String line = sysin.readLine();
//						try {
//							int tnum = Integer.valueOf(line.trim());
//							simThreadsCount = tnum;
//						}
//						catch(Exception e) {
//							// 
//						}
//					}
//					
//					
//					while(true) {
//						AccessSimulator accSim = new AccessSimulator();
//
//						CompressedBinaryStore.INFLATE = CompressedBinaryStore.INFLATE.copy();
//						CompressedBinaryStore.DEFLATE = CompressedBinaryStore.DEFLATE.copy();
//						CompressedBinaryStore.ITEM_SIZE = CompressedBinaryStore.ITEM_SIZE.copy();
//						CompressedBinaryStore.BYTE_SIZE = CompressedBinaryStore.BYTE_SIZE.copy();
//						CompressedBinaryStore.RATIO = CompressedBinaryStore.RATIO.copy();						
//						
//						accSim.accessThreads = simThreadsCount;
//						accSim.accumRangeStart = generator.getFirstAccumId();
//						accSim.accumRangeEnd = accSim.accumRangeStart + generator.getAccumCount();
//						accSim.readRatio = Double.parseDouble(System.getProperty("test.readRation", "0.5")); 
//						accSim.batchRatio = Double.parseDouble(System.getProperty("test.batchRation", "0.25")); 
//						
//						accSim.start(cache);
//
//						System.out.println("suspended");
//						String line = sysin.readLine();
//						try {
//							int tnum = Integer.valueOf(line.trim());
//							simThreadsCount = tnum;
//							continue;
//						}
//						catch(Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
			}
			
			while(true) {
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	};

}
