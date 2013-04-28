package org.gridkit.coherence.cachewatchdog;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

public class PartitionMonitorTest_Statics {

	private static PartitionMonitorTest_Statics INSTANCE = new PartitionMonitorTest_Statics();
	
	public static PartitionMonitorTest_Statics getInstance() {
		return INSTANCE;
	}

	public static void reset() {
		INSTANCE = new PartitionMonitorTest_Statics();
	}
	
	public CountDownLatch[] latches;
	public AtomicInteger[] counters;
	
	public void initPartitionCounter(int n) {
		latches = new CountDownLatch[n];
		for(int i = 0; i != n; ++i) {
			latches[i] = new CountDownLatch(1);
		}
		counters = new AtomicInteger[n];
		for(int i = 0; i != n; ++i) {
			counters[i] = new AtomicInteger();
		}
	}
	
	public void touchPartition(int n) {
		counters[n].incrementAndGet();
		latches[n].countDown();
	}
	
	public void waitAllLatches() throws InterruptedException {
		for(CountDownLatch latch: latches) {
			latch.await();
		}
	}
	
	public void verifyCounters(int value) {
		for(int i = 0; i != counters.length; ++i) {
			Assert.assertEquals("Check counter for partition [" + i + "]", value, counters[i].get());
		}
	}

	public int getCounterDiscrepancy(int value1, int value2) {
		int v2count = 0;
		for(int i = 0; i != counters.length; ++i) {
			if (counters[i].get() == value1) {
				continue;
			}
			++v2count;
			Assert.assertTrue("Check counter for partition [" + i + "] eigther " + value1 + " or " + value2, value2 == counters[i].get());
		}
		return v2count;
	}
}
