package org.gridkit.coherence.offheap.storage.memlog;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

public class PagedMemoryBinaryStoreManagerTest {

	@Test
	public void test_basics() {
		
		PageLogManager pageManager = new PageLogManager(8 << 10, 16);
		
		PagedMemoryBinaryStoreManager storeMan = new PagedMemoryBinaryStoreManager("test_basics", pageManager);
		
		BinaryStore store = storeMan.create();
	
		{
			ByteChunk key = new ByteChunk("AAAA".getBytes());
			ByteChunk value = new ByteChunk("BBBB".getBytes());
			
			store.put(key, value);
			
			ByteChunk value2 = store.get(key);
			
			Assert.assertTrue(value.sameBytes(value2));
	
			ByteChunk value3 = new ByteChunk("CCCC".getBytes());
			store.put(key, value3);
			
			value2 = store.get(key);
			Assert.assertTrue(value3.sameBytes(value2));
			
			store.remove(key);
			
			Assert.assertTrue(store.get(key) == null);
		}
	
		{
			ByteChunk key1 = new ByteChunk(new byte[8]); // this way we can fabricate same hash codes
			ByteChunk key2 = new ByteChunk(new byte[9]); // this way we can fabricate same hash codes
			key1.putInt(0, -1);
			key1.putInt(4, 100);
			key2.putInt(0, -1);
			key2.set(4, (byte) 0x00);
			key2.putInt(5, 100);
			
			store.put(key1, key1);
			store.put(key2, key2);
			
			ByteChunk val;
			val = store.get(key1);
			Assert.assertTrue(key1.sameBytes(val));
			
			val = store.get(key2);
			Assert.assertTrue(key2.sameBytes(val));
			
			store.remove(key1);
			Assert.assertTrue(store.get(key1) == null);

			val = store.get(key2);
			Assert.assertTrue(key2.sameBytes(val));
		}
		
		storeMan.close();
	}
	
	@Test
	public void large_consistency_test_basics() {
		
		PageLogManager pageManager = new PageLogManager(64 << 10, 256);
		
		PagedMemoryBinaryStoreManager storeMan = new PagedMemoryBinaryStoreManager("test_basics", pageManager);
		
		BinaryStore store = storeMan.create();
		
		Random rnd = new Random(1);
		Map<String, String> refMap = new HashMap<String, String>();
		
		int objNum = 10000;
		int holeNum = 2000;
		
		for(int n = 0; n != 100000; ++n) {

			if (n > 85000 && objNum > 0) {
				--objNum;
				++holeNum;
			}
			
			if (n % 5000 == 0) {
				compare(refMap, store, objNum, holeNum);
			}
			
			int size = refMap.size(); 
			if (size > objNum) {
 				if (n > 85000 || ((size - objNum) >= rnd.nextInt(holeNum))) {
					while(true) {
						String key;
						if (size < (objNum + holeNum ) / 8) {
							key = refMap.keySet().iterator().next();
						}
						else{							
							key = randomKey(rnd, objNum, holeNum);
						}
						boolean hit = refMap.remove(key) != null;
						store.remove(toByteChunk(key));
						if (hit) {
							break;
						}
					}
					continue;
				}
			}

			String key = randomKey(rnd, objNum, holeNum);
			String val = randomString(rnd.nextInt(10) + 20, rnd);
			
			refMap.put(key, val);
			store.put(toByteChunk(key), toByteChunk(val));
		}
		
		compare(refMap, store, objNum, holeNum);
		
		storeMan.close();
	}

	private static void compare(Map<String, String> ref, BinaryStore store, int objNum, int holeNum) {
		for(int i = 0; i != objNum + holeNum; ++i) {
			String key = String.valueOf(100000l + i);
			
			String val = ref.get(key);
			ByteChunk bval = store.get(toByteChunk(key));
			
			if (val == null) {
				Assert.assertTrue(bval == null);
			}
			else {
				Assert.assertTrue(bval.sameBytes(toByteChunk(val)));
			}
		}
	}

	private static ByteChunk toByteChunk(String val) {
		return new ByteChunk(val.getBytes());
	}
	
	private static String randomKey(Random rnd, int objNum, int holeNum) {
		long key = 100000 + rnd.nextInt(objNum + holeNum);
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
