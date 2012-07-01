package org.gridkit.gatling.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;


public class SpeedLimitTest {

	@Test
	public void test_UltraHighRate_1_thread() {
		double targetRate = 10000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 1, (int)(targetRate * 10));
		System.out.println(String.format("Thread 1, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_HighRate_1_thread() {
		double targetRate = 1000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 1, (int)(targetRate * 10));
		System.out.println(String.format("Thread 1, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_MidRate_1_thread() {
		double targetRate = 100;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 1, (int)(targetRate * 10));
		System.out.println(String.format("Thread 1, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}	

	@Test
	public void test_LowRate_1_thread() {
		double targetRate = 5;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 1, (int)(targetRate * 10));
		System.out.println(String.format("Thread 1, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}	
	
	@Test
	public void test_SlowestRate_1_thread() {
		double targetRate = 0.5;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 1, (int)(targetRate * 10));
		System.out.println(String.format("Thread 1, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}	
	
	@Test
	public void test_UltraHighRate_16_thread() {
		double targetRate = 10000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 16, (int)(targetRate * 10));
		System.out.println(String.format("Thread 16, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_HighRate_16_thread() {
		double targetRate = 1000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 16, (int)(targetRate * 10));
		System.out.println(String.format("Thread 16, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_MidRate_16_thread() {
		double targetRate = 100;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 16, (int)(targetRate * 10));
		System.out.println(String.format("Thread 16, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_LowRate_16_thread() {
		double targetRate = 5;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 16, (int)(targetRate * 10));
		System.out.println(String.format("Thread 16, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_SlowestRate_16_thread() {
		double targetRate = 0.5;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 16, (int)(targetRate * 10));
		System.out.println(String.format("Thread 16, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_UltraHighRate_4_thread() {
		double targetRate = 10000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 4, (int)(targetRate * 10));
		System.out.println(String.format("Thread 4, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}
	
	@Test
	public void test_HighRate_4_thread() {
		double targetRate = 1000;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 4, (int)(targetRate * 10));
		System.out.println(String.format("Thread 4, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}

	@Test
	public void test_MidRate_4_thread() {
		double targetRate = 100;
		SpeedLimit limit = SpeedLimit.Helper.newSpeedLimit(targetRate);
		double rate = testSpeedLimit_unbalanced(limit, 4, (int)(targetRate * 10));
		System.out.println(String.format("Thread 4, target rate %f -> %f (error %.3f%%)", targetRate, rate, Math.abs(100 * (targetRate - rate) / targetRate)));
		assertError(rate, targetRate, 0.05);
	}
	
	private void assertError(double value, double target, double tolerance) {
		AssertJUnit.assertTrue(String.format("%f within %.3f bounds from %f", value, tolerance, target), value < (target + target * tolerance) && value > (target - target * tolerance));
	}
	
	@SuppressWarnings("unused")
	private double testSpeedLimit_balanced(final SpeedLimit limit, int threadCount, int events) {
		long start = System.nanoTime();
		final CountDownLatch barrier = new CountDownLatch(threadCount);
		final int eventsPerThread = events / threadCount;
		
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for(int i = 0; i != threadCount; ++i) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					for(int i = 0; i != eventsPerThread; ++i) {
						limit.accure();
					}
					barrier.countDown();
				}
			});
		}
		
		try {
			barrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long time = System.nanoTime() - start;
		executor.shutdown();

		double rate = 1d * TimeUnit.SECONDS.toNanos(1) * eventsPerThread * threadCount / time;

		return rate;		
	}

	private double testSpeedLimit_unbalanced(final SpeedLimit limit, int threadCount, int events) {
		
		long start = System.nanoTime();
		final AtomicInteger counter = new AtomicInteger(events + 1);
		final CountDownLatch finishBarrier = new CountDownLatch(threadCount);

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		for(int i = 0; i != threadCount; ++i) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					while(true) {
						if (counter.decrementAndGet() < 0) {
							break;
						}
						else {
							limit.accure();
						}
					}
					finishBarrier.countDown();
				}
			});
		}
		
		try {
			finishBarrier.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long time = System.nanoTime() - start;
		executor.shutdown();

		double rate = 1d * TimeUnit.SECONDS.toNanos(1) * events / time;

		return rate;		
	}
	
	public static void main(String[] args) {
		SpeedLimitTest test = new SpeedLimitTest();
		
		test.test_UltraHighRate_1_thread();
		test.test_HighRate_1_thread();
		test.test_MidRate_1_thread();
		test.test_LowRate_1_thread();
		test.test_SlowestRate_1_thread();

		test.test_UltraHighRate_16_thread();
		test.test_HighRate_16_thread();
		test.test_MidRate_16_thread();
		test.test_LowRate_16_thread();
		test.test_SlowestRate_16_thread();
		
		test.test_UltraHighRate_4_thread();
		test.test_HighRate_4_thread();
		test.test_MidRate_4_thread();
	}
}
