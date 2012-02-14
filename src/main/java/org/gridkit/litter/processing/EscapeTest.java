package org.gridkit.litter.processing;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Arrays;
import java.util.Random;

public class EscapeTest {
	
	public static void main(String[] args) {
//		StringFactory sf = new SimpleStringFactory(32);
		StringFactory sf = new FixedStringFactory(32);
		
		TestRunner runner = new TestRunner(sf, 4);
		runner.run();
	}

	public static class TestRunner {
		
		private StringFactory stringFactory;
		private int batchSize = 5000;
		private int testLenght;
		
		public TestRunner(StringFactory stringFactory, int testLenght) {
			this.stringFactory = stringFactory;
			this.testLenght = testLenght;
		}

		public void run() {
			
			getYoungGCCount();
			getYoungSpaceSize();
			
			Random rnd = new Random(0);
			
			{ // warm up
			
				for(int i = 0; i != 20000; ++i) {
					String val = stringFactory.randomString(rnd, testLenght);
					(val += " ").toString();
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
	
				for(int i = 0; i != 20000; ++i) {
					String val = stringFactory.randomString(rnd, testLenght);
					(val += " ").toString();
				}
	
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			
			long cummulativeSpaceUsage = 0;
			long lastSpaceSize = getYoungSpaceSize();
			long lastGCCount = getYoungGCCount();
			long cummulativeOperationCount = 0;
			
			long initGCCount = getYoungGCCount();
			long totalOpCount = 0;

			String lastValue = "";
			rnd = new Random(0);
			
			int n = 0;
			while(true) {
				++n;

				lastGCCount = getYoungGCCount();
				lastSpaceSize = getYoungSpaceSize();
				while(lastGCCount != getYoungGCCount()) {
					lastGCCount = getYoungGCCount();
					lastSpaceSize = getYoungSpaceSize();
				}

				for(int i = 0; i != batchSize; ++i) {
					lastValue = stringFactory.randomString(rnd, testLenght);
				}
				
				long newSpaceSize = getYoungSpaceSize();
				long newGCCount = getYoungGCCount();
				
				if (newGCCount == lastGCCount) {
					cummulativeSpaceUsage += newSpaceSize - lastSpaceSize;
					cummulativeOperationCount += batchSize;
				}
				
				totalOpCount += batchSize;
				
				if (totalOpCount % 10000000 == 0) {
					System.out.println(String.format("\nIteration %d", n));
					long totalCollections = newGCCount - initGCCount;
					System.out.println(String.format("Collections: Young GC %d | Operations %dk | Rate %dk (ops/collection)", totalCollections, totalOpCount >> 10, (totalOpCount / totalCollections) >> 10));
					if (cummulativeOperationCount > 0) {
						System.out.println(String.format("Allcoation:  Space %dM | Operations %dk | %d bytes per operation", cummulativeSpaceUsage >> 20, cummulativeOperationCount >> 10, cummulativeSpaceUsage / cummulativeOperationCount));
					}
					else {
						System.out.println(String.format("Allcoation:  N/A"));
					}
					cummulativeSpaceUsage >>= 4;
					cummulativeOperationCount >>= 4;
					System.out.println(String.format("Last string: " + lastValue));
				}
			}
		}
		
		long getYoungGCCount() {
			for(GarbageCollectorMXBean mbean: ManagementFactory.getGarbageCollectorMXBeans()) {
				if (Arrays.asList("Copy", "PS Scavenge").contains(mbean.getName())) {
					return mbean.getCollectionCount();
				}
			}
			throw new IllegalArgumentException("Unknown GC");
		}
		
		long getYoungSpaceSize() {
			for(MemoryPoolMXBean mbean: ManagementFactory.getMemoryPoolMXBeans()) {
				if (Arrays.asList("Eden Space", "PS Eden Space").contains(mbean.getName())) {
					return mbean.getUsage().getUsed();
				}
			}
			throw new IllegalArgumentException("Unknown GC");
		}
	}
	
	
	public static abstract class StringFactory {
		
		public abstract String randomString(Random rnd, int lenght);
		
	}
	
	public static class SimpleStringFactory extends StringFactory {

		private int capacity;
		
		public SimpleStringFactory(int capacity) {
			this.capacity = capacity;
		}
		
		@Override
		public String randomString(Random rnd, int lenght) {
			StringBuilder sb = new StringBuilder(capacity);
			for(int i = 0; i != lenght; ++i) {
				if (i != 0) {
					sb.append(' ');
				}
				sb.append(rnd.nextInt(10000));
			}
			return sb.toString();
		}
	}

	public static class FixedStringFactory extends StringFactory {
		
		private int capacity;
		
		public FixedStringFactory(int capacity) {
			this.capacity = capacity;
		}

		@Override
		public String randomString(Random rnd, int lenght) {
			return randomString_inline(rnd, lenght);
		}

		public String randomString_FB(Random rnd, int lenght) {
			FixedStringBuffer sb = new FixedStringBuffer(capacity);
			for(int i = 0; i != lenght; ++i) {
				if (i != 0) {
					sb.append(' ');
				}
//				sb.append(rnd.nextInt(10000));
				sb.append(Integer.toString(rnd.nextInt(10000)));
			}
			return sb.toString();
		}

		public String randomString_inline(Random rnd, int lenght) {			
			char[] fixBuf = new char[32];
			int pos = 0;
			for(int i = 0; i != lenght; ++i) {
				if (i != 0) {
					pos = FixedStringBuffer.append(fixBuf, pos, ' ');
				}
				pos = FixedStringBuffer.append(fixBuf, pos, rnd.nextInt(10000));
			}
			return new String(fixBuf, 0, pos);
		}
	}
	
	public static class FixedStringBuffer {
		
		private char[] buffer;
		private int lenght;
		
		public FixedStringBuffer(int limit) {
			buffer = new char[limit];
			lenght = 0;
		}
		
		public FixedStringBuffer append(int i) {
	        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
	        getChars(i, lenght + size, buffer);
	        lenght += size;
	        return this;
		}
		
		public static int append(char[] buf, int pos, int i) {
	        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
	        getChars(i, pos + size, buf);
	        pos += size;
	        return pos;			
		}

		public FixedStringBuffer append(char ch) {
			buffer[lenght] = ch;
			++lenght;
			return this;
		}

		public static int append(char[] buf, int pos, char ch) {
			buf[pos] = ch;
			++pos;
			return pos;
		}

		public FixedStringBuffer append(String str) {
			for(int i = 0; i != str.length(); ++i) {
				buffer[lenght++] = str.charAt(i);
			}
			return this;
		}
		
		public String toString() {
			return new String(buffer, 0, lenght);
		}		
		
	    final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
            99999999, 999999999, Integer.MAX_VALUE };

		// Requires positive x
		static int stringSize(int x) {
			for (int i=0; ; i++)
			if (x <= sizeTable[i])
			return i+1;
		}

	    final static char[] digits = {
	    	'0' , '1' , '2' , '3' , '4' , '5' ,
	    	'6' , '7' , '8' , '9' , 'a' , 'b' ,
	    	'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
	    	'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
	    	'o' , 'p' , 'q' , 'r' , 's' , 't' ,
	    	'u' , 'v' , 'w' , 'x' , 'y' , 'z'
	    };

		
	    final static char [] DigitOnes = { 
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	    };
	    
	    final static char [] DigitTens = {
	    	'0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
	    	'1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
	    	'2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
	    	'3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
	    	'4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
	    	'5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
	    	'6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
	    	'7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
	    	'8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
	    	'9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	    }; 
		
	    static void getChars(int i, int index, char[] buf) {
	        int q, r;
	        int charPos = index;
	        char sign = 0;

	        if (i < 0) { 
	            sign = '-';
	            i = -i;
	        }

	        // Generate two digits per iteration
	        while (i >= 65536) {
	            q = i / 100;
	        // really: r = i - (q * 100);
	            r = i - ((q << 6) + (q << 5) + (q << 2));
	            i = q;
	            buf [--charPos] = DigitOnes[r];
	            buf [--charPos] = DigitTens[r];
	        }

	        // Fall thru to fast mode for smaller numbers
	        // assert(i <= 65536, i);
	        for (;;) { 
	            q = (i * 52429) >>> (16+3);
	            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
	            buf [--charPos] = digits [r];
	            i = q;
	            if (i == 0) break;
	        }
	        if (sign != 0) {
	            buf [--charPos] = sign;
	        }
	    }
	}

}
