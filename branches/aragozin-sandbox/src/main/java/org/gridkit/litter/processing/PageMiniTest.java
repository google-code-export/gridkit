package org.gridkit.litter.processing;

import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.offheap.storage.memlog.BinaryKeyValueStore;
import org.gridkit.coherence.offheap.storage.memlog.ByteChunk;
import org.gridkit.coherence.offheap.storage.memlog.InHeapBinaryStoreManager2;
import org.gridkit.coherence.offheap.storage.memlog.OffHeapBinaryStoreManager2;

import com.tangosol.io.journal.JournalBinaryStore;
import com.tangosol.io.journal.RamJournalRM;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryRadixTree;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("unchecked")
public class PageMiniTest {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}	

	private static BinaryKeyValueStore CACHE;
	static {
		if ("ramjounal".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using Coherence RAM jounral");
			CACHE = new RJStore();
		}
		else if ("large".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using paged storage [8G / 64M page / 64k segments]");
			CACHE = new InHeapBinaryStoreManager2("test", 64 << 20, 8l << 30).createKeyValueStore(64 << 10);
		}
		else if ("large-256".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using paged storage [8G / 256M page / 64k segments]");
			CACHE = new InHeapBinaryStoreManager2("test", 256 << 20, 8l << 30).createKeyValueStore(64 << 10);
		}
		else if ("x-large-256".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using paged storage [30G / 256M page / 64k segments]");
			CACHE = new InHeapBinaryStoreManager2("test", 256 << 20, 30l << 30).createKeyValueStore(64 << 10);
		}
		else if ("large-off-heap".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using paged storage [8G / 64M page / 64k segments]");
			CACHE = new OffHeapBinaryStoreManager2("test", 64 << 20, 8l << 30).createKeyValueStore(64 << 10);
		}
		else if ("x-large-off-heap".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using paged storage [30G / 64M page / 64k segments]");
			CACHE = new OffHeapBinaryStoreManager2("test", 64 << 20, 30l << 30).createKeyValueStore(64 << 10);
		}
		else if ("off-heap".equals(System.getProperty("litter.map-type"))) {
			System.out.println("Using off-heap paged storage [576M / 4M page]");
			CACHE = new OffHeapBinaryStoreManager2("test", 4 << 20, 576l << 20).createKeyValueStore();
		}
		else {
			System.out.println("Using paged storage [576M / 4M page]");
			CACHE = new InHeapBinaryStoreManager2("test", 4 << 20, 576 << 20).createKeyValueStore();
		}
	}
//	private static BinaryStore CACHE = new InHeapBinaryStoreManager2("test", 4 << 20, 576 << 20).create();
//	private static Map<Object, Object> CACHE;
//	static {
//		try {
////			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "java.util.HashMap")).newInstance();
//			CACHE = (Map<Object, Object>) Class.forName(System.getProperty("litter.test-map", "org.gridkit.litter.processing.PagedBinaryPackedMap")).newInstance();
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
	private static int OBJECT_SIZE = Integer.getInteger("litter.object-size", 16);
	private static int OBJECT_SIZE_DEVIATION = Integer.getInteger("litter.object-size-dev", 10);
//	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 2000000);
	private static int NUMBER_OF_OBJECTS = Integer.getInteger("litter.number-of-objects", 8000000);
	private static int NUMBER_OF_HOLES = Integer.getInteger("litter.number-of-holes", NUMBER_OF_OBJECTS / 4);
	private static int REPORTING_INTERVAL = Integer.getInteger("litter.reporting-interval", 500000);
	
	
	public static void main(String[] args) {
		
		System.out.println("Object size: " + OBJECT_SIZE + "(-/+" + OBJECT_SIZE_DEVIATION + ")");		
		System.out.println("Number of objects: " + NUMBER_OF_OBJECTS + "(+ " + NUMBER_OF_HOLES + " holes)");		
	
//		PauseDetector.activate();
		
		
		rnd = new Random(1);
		
		size = 0;
		while(size < NUMBER_OF_OBJECTS) {
			String key = randomKey(rnd);
			if (CACHE.get(new ByteChunk(key.getBytes())) == null) {
				++size;
			}
			CACHE.put(new ByteChunk(key.getBytes()), new ByteChunk(randomString(rnd).getBytes()));
			if (size % 100000 == 0) {
				System.out.println("Done " + size);
			}
		}

		System.out.println("Initial loading complete");
		System.gc();
		
		tickCounter = 0;
		startTime = System.nanoTime();
		while(true) {
			loop();
		}		
	}
	
	private static void loop() {
		while(true) {
			long ss = System.nanoTime();
			
			if (size > NUMBER_OF_OBJECTS) {
				if ((size - NUMBER_OF_OBJECTS) >= rnd.nextInt(NUMBER_OF_HOLES)) {
					String key = randomKey(rnd);
					if (CACHE.get(new ByteChunk(key.getBytes())) != null) {
						--size;
						CACHE.remove(new ByteChunk(key.getBytes()));
						continue;
					}
				}
			}
	
			String key = randomKey(rnd);
			if (CACHE.get(new ByteChunk(key.getBytes())) == null) {
				++size;
			}
			CACHE.put(new ByteChunk(key.getBytes()), new ByteChunk(randomString(rnd).getBytes()));
			
			tickCounter++;
			
			long st = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ss);
			if (st > 30 ) {
				System.err.println("Slow operation: " + st + "ms");
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

	private static String randomString(Random rnd) {
		int size = OBJECT_SIZE - OBJECT_SIZE_DEVIATION + rnd.nextInt(2 * OBJECT_SIZE_DEVIATION);
		String str = randomString(size, rnd);
		return str;
	}
	
	private static String randomKey(Random rnd) {
		long key = 100000 + rnd.nextInt(NUMBER_OF_OBJECTS + NUMBER_OF_HOLES);
		return String.valueOf(key);
	}

	static char[] CHARS_BUFFER = new char[256 * 1024];
	private static Random rnd;
	private static int size;
	private static long tickCounter;
	private static long startTime;
	public static String randomString(int len, Random rnd) {
		if (len > CHARS_BUFFER.length || len < 0) {
			throw new IllegalArgumentException("String length exceeds buffer size");
		}
		for(int i = 0; i != len; ++i) {
			CHARS_BUFFER[i] = (char)('A' + rnd.nextInt(23));
//			CHARS_BUFFER[i] = (char)('A');
		}
		return new String(CHARS_BUFFER, 0, len);
	}
	
	public static class RJStore implements BinaryKeyValueStore {

		private JournalBinaryStore bstore;
		
		public RJStore() {
			RamJournalRM rj = new RamJournalRM();
			rj.start();
			bstore = rj.createBinaryStore();
		}
		
		@Override
		public ByteChunk get(ByteChunk key) {
			Binary bin = toBinary(key);
			Binary val = bstore.load(bin);			
			return val == null ? null : new ByteChunk(val.toByteArray());
		}

		private Binary toBinary(ByteChunk key) {
			return new Binary(key.array(), key.offset(), key.lenght());
		}

		@Override
		public Iterator<ByteChunk> keys() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void put(ByteChunk key, ByteChunk value) {
			bstore.store(toBinary(key), toBinary(value));
		}

		@Override
		public void remove(ByteChunk key) {
			bstore.erase(toBinary(key));
			
		}

		@Override
		public int size() {
			return -1;
		}

		@Override
		public void clear() {
		}
	}

}
