package org.gridkit.litter.processing;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("unchecked")
public class PageMiniTest2 {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}	
	
	private static Map<Object, Object> CACHE;
	static {
		try {
//			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "java.util.HashMap")).newInstance();
			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "org.gridkit.litter.processing.PagedBinaryPackedMap")).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private static int OBJECT_SIZE = Integer.getInteger("litter.object-size", 16);
	private static int OBJECT_SIZE_DEVIATION = Integer.getInteger("litter.object-size-dev", 10);
	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 2000000);
	private static int NUMBER_OF_HOLES = Integer.getInteger("litter.number-of-holes", NUMBER_OF_OBJECTS / 4);
	private static int REPORTING_INTERVAL = Integer.getInteger("litter.reporting-interval", 1000000);
	
	
	public static void main(String[] args) {
		
		System.out.println("Object size: " + OBJECT_SIZE + "(-/+" + OBJECT_SIZE_DEVIATION + ")");		
		System.out.println("Number of objects: " + NUMBER_OF_OBJECTS + "(+ " + NUMBER_OF_HOLES + " holes)");		
	
//		PauseDetector.activate();
		
		int limit = NUMBER_OF_OBJECTS + NUMBER_OF_HOLES; 
		
		Random rnd = new Random(1);
		
		int size = 0;
		while(size < NUMBER_OF_OBJECTS) {
			String key = randomKey(rnd);
			if (CACHE.put(key, randomString(rnd)) == null) {
				++size;
			}
		}

		System.out.println("Initial loading complete");
		System.gc();
		
		long tickCounter = 0;
		long startTime = System.nanoTime();
		while(true) {

			if (size > NUMBER_OF_OBJECTS) {
				if ((size - NUMBER_OF_OBJECTS) >= rnd.nextInt(NUMBER_OF_HOLES)) {
					String key = randomKey(rnd);
					if (CACHE.remove(key) != null) {
						--size;
						continue;
					}
				}
			}

			String key = randomKey(rnd);
			if (CACHE.put(key, randomString(rnd)) == null) {
				++size;
			}
			
			tickCounter++;
			
			if (tickCounter % REPORTING_INTERVAL == 0) {
				long time = System.nanoTime() - startTime;
				println("Iteration " + tickCounter);
				double pace = (double)REPORTING_INTERVAL / time * TimeUnit.SECONDS.toNanos(1);
				println("Tick speed: " + pace);
				long memUsage = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
				println("Mem usage: " + (memUsage >> 10) + "k");				
				startTime = System.nanoTime();
			}			
		}		
	}

	private static String randomString(Random rnd) {
		int size = OBJECT_SIZE - OBJECT_SIZE_DEVIATION + rnd.nextInt(2 * OBJECT_SIZE_DEVIATION);
		String str = randomString(size, rnd);
		return str;
	}
	
	private static String randomKey(Random rnd) {
		long key = 100000 + rnd.nextInt(NUMBER_OF_OBJECTS + NUMBER_OF_HOLES);
		return String.valueOf(key);
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
