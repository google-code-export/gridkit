package org.gridkit.litter.processing;

import java.util.Date;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.gridkit.litter.utils.PauseDetector;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class MiniTest {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}	
	
//	private static SortedMap<Long, Object> QUEUE = new TreeMap<Long, Object>();
	private static SortedMap<Long, Object> QUEUE = new ConcurrentSkipListMap<Long, Object>();
//	private static int OBJECT_SIZE = Integer.getInteger("litter.object-size", 16);
	private static int OBJECT_SIZE = Integer.getInteger("litter.object-size", 32);
	private static int OBJECT_SIZE_DEVIATION = Integer.getInteger("litter.object-size-dev", 10);
	private static int TIME_TO_LIFE = Integer.getInteger("litter.time-to-live", 1000000);
	private static int TIME_TO_LIFE_DEVIATION = Integer.getInteger("litter.time-to-live.dev", 3 * TIME_TO_LIFE);
	private static int REPORTING_INTERVAL = Integer.getInteger("litter.reporting-interval", 1000000);
	
	
	private static long tickCounter;
	private static Random rnd;
	private static long startTime;


	public static void main(String[] args) {
		
//		Thread.currentThread().
		System.out.println("Object size: " + OBJECT_SIZE + "(-/+" + OBJECT_SIZE_DEVIATION + ")");		
		System.out.println("Time to live: " + TIME_TO_LIFE + "(dev: " + TIME_TO_LIFE_DEVIATION + ")");		
	
//		PauseDetector.activate();
		
		rnd = new Random(1);
		tickCounter = 0;
		startTime = System.nanoTime();
		while(true) {
			testLoop();			
		}		
	}

	private static void testLoop() {
		while(true) {
			long nt = System.nanoTime();
			int size = OBJECT_SIZE - OBJECT_SIZE_DEVIATION + rnd.nextInt(2 * OBJECT_SIZE_DEVIATION);
			String str = randomString(size, rnd);
			long stamp = (long)(tickCounter + (TIME_TO_LIFE_DEVIATION * rnd.nextGaussian()));
			QUEUE.put(stamp, str);
			
			if (tickCounter % 1000 == 0) {
				long cutOff = tickCounter - TIME_TO_LIFE;
				QUEUE.subMap(Long.MIN_VALUE, cutOff).clear();
			}
			
			tickCounter++;
			long opt = System.nanoTime() - nt;
			
			if (TimeUnit.NANOSECONDS.toMillis(opt) > 5) {
				System.out.println("Warning slow operation: " + TimeUnit.NANOSECONDS.toMillis(opt) + "ms");
			}
			
			if (tickCounter % REPORTING_INTERVAL == 0) {
				long time = System.nanoTime() - startTime;
				println("Iteration " + tickCounter);
				double pace = (double)REPORTING_INTERVAL / time * TimeUnit.SECONDS.toNanos(1);
				println("Tick speed: " + pace);
				long memUsage = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
				println("Mem usage: " + (memUsage >> 10) + "k");				
				startTime = System.nanoTime();
				break;
			}
		}
	}
	
	static char[] CHARS_BUFFER = new char[1024];
	public static String randomString(int len, Random rnd) {
		if (len > 1024 || len < 0) {
			throw new IllegalArgumentException("String length exceeds buffer size");
		}
		for(int i = 0; i != len; ++i) {
			CHARS_BUFFER[i] = (char)('A' + rnd.nextInt(23));
		}
		return new String(CHARS_BUFFER, 0, len);
	}

}
