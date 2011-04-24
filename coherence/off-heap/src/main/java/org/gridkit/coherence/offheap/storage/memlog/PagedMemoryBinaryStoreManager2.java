package org.gridkit.coherence.offheap.storage.memlog;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PagedMemoryBinaryStoreManager2 implements BinaryStoreManager {

	private static int EMPTY = 0;
	private static int LOCKED = 1;
	
	private static int ALLOC_NEW_VALUE = 0;
	private static int ALLOC_NEW_LIST = 0;
	private static int ALLOC_RELOCATE_VALUE = 1;
	private static int ALLOC_RELOCATE_LIST = 1;
	
	private static long TABLE_SERVICE_PERIOD = TimeUnit.SECONDS.toNanos(30);
//	private static long MEM_DIAG_REPORT_PERIOD = TimeUnit.SECONDS.toNanos(30);
	private static long MEM_DIAG_REPORT_PERIOD = TimeUnit.SECONDS.toNanos(10);
	
	private final String name;
	private List<BinaryHashTable> tables = new ArrayList<BinaryHashTable>();
	private PageLogManager pageManager;
	private Thread maintenanceDaemon;
	
	public PagedMemoryBinaryStoreManager2(String name, PageLogManager pageManager) {
		this.name = name;
		this.pageManager = pageManager;
		this.maintenanceDaemon = createMaintenanceThread();
	}
	
	private Thread createMaintenanceThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				maintenanceCycle();
			}
		});
		thread.setName("PagedMemoryBinaryStore-" + name + "-ServiceThread");
		thread.setDaemon(true);
		return thread;
	}

	@Override
	public synchronized BinaryStore create() {
		BinaryHashTable hash = new BinaryHashTable();
		tables.add(hash);
		if (maintenanceDaemon.getState() == State.NEW) {
			maintenanceDaemon.start();
		}
		return hash;
	}

	@Override
	public synchronized void destroy(BinaryStore store) {
		BinaryHashTable hash = (BinaryHashTable) store;
		// TODO check owner
		int n = tables.indexOf(store);
		tables.remove(n);
		hash.clear();
	}
	
	@SuppressWarnings("deprecation")
	public synchronized void close() {
		List<BinaryHashTable> tables = new ArrayList<BinaryHashTable>(this.tables);
		for(BinaryHashTable table: tables) {
			destroy(table);
		}
		if (maintenanceDaemon.getState() != State.NEW) {
			// TODO graceful death
			maintenanceDaemon.stop();
//			try {
//				maintenanceDaemon.join();
//			} catch (InterruptedException e) {
//				// ignore
//			}
		}
	}

	private void maintenanceCycle() {
		int n = 0;
		int idle = 0;
		long diagTimestamp = System.nanoTime();
		while(true) {
			if (diagTimestamp + MEM_DIAG_REPORT_PERIOD < System.nanoTime()) {
				pageManager.dumpStatistics();
				synchronized (this) {
					int x = 0;
					for(BinaryHashTable table : tables) {
						StringBuilder buf = new StringBuilder();
						buf.append("Hashtable #" + x).append("\n");
						buf.append("Size: ").append(table.size.get()).append("\n");
						buf.append("Capacity: ").append(table.capacity).append("\n");
						buf.append("Load factor: ").append(String.format("%f", 1.0d * table.size.get() / table.capacity)).append("\n");
						System.out.println(buf.toString());
						++x;
					}
				}
				diagTimestamp = System.nanoTime();
			}
			if (idle > 10) {
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
				idle = 0;
			}
			BinaryHashTable table;
			synchronized(this) {
				if (tables.size() > n) {
					table = tables.get(0);
				}
				else {
					n = 0;
					continue;
				}
			}	
			if (table.maintenanceTimestamp + TABLE_SERVICE_PERIOD > System.nanoTime()) {
				++idle;
				continue;
			}
			else {
				doTableMaintenance(table);
			}
			++n;
		}
	}

	private void doTableMaintenance(BinaryHashTable table) {
		int n = 0;
		
		try {
			table.checkTablePhysicalSize();		
			refresh:
			while(true) {
				table.tableLock.readLock().lock();
				try {
					for(int i = 0; i != 16; ++i) {
						table.recycleEntry(n++);
						if (n > table.capacity) {
							break refresh;
						}
					}
				}
				finally {
					table.tableLock.readLock().unlock();
				}
				if (n % (1 << 10) == 0) {
					table.checkTablePhysicalSize();
				}
			}	
			
			table.maintenanceTimestamp = System.nanoTime();
		}
		catch(NullPointerException e) {
			// may happen if table is erased
		}
	}

	private class BinaryHashTable implements BinaryStore {

		AtomicIntegerArray hashtable = new AtomicIntegerArray(1024);
		AtomicIntegerArray locktable = createLocktable(hashtable.length());
		volatile int capacity = hashtable.length() >> 1;
		ReadWriteLock tableLock = new ReentrantReadWriteLock();

		AtomicInteger size = new AtomicInteger();
		float targetLoadFactor = 0.8f;
		float thresholdLoadFactor = 0.99f;
		
		long maintenanceTimestamp = System.nanoTime();

		// lock assumed
		private int[] getEntries(int index) {
			int pointer;
			pointer = hashtable.get(index);
			if (pointer == 0) {
				return null;
			}
			else if (pointer > 0) {
				return new int[]{pointer};
			}
			else {
				pointer = -pointer;
				ByteChunk chunk = pageManager.get(pointer);
				int[] entries = new int[chunk.lenght() / 4];
				for(int i = 0; i != entries.length; ++i) {
					entries[i] = chunk.intAt(i * 4);
				}
				return entries;
			}
		}
		
		// lock assumed
		private void setEntries(int index, int[] entries) {
			int pointer;
			pointer = hashtable.get(index);
			if (pointer != EMPTY && pointer < 0) {
				pointer = -pointer;
				pageManager.release(pointer);
			}
			if (entries == null || entries.length == 0) {
				hashtable.set(index, EMPTY);
			}
			else if (entries.length == 1) {
				hashtable.set(index, entries[0]);
			}
			else {
				int npp = pageManager.allocate(4 * entries.length, ALLOC_NEW_LIST);
				ByteChunk list = pageManager.get(npp);
				for(int i = 0; i != entries.length; ++i) {
					list.putInt(4 * i, entries[i]);
				}
				hashtable.set(index, -npp);
			}
		}

		public void clear() {
			tableLock.writeLock().lock();
			try {
			
				for(int i = 0; i != capacity; ++i) {
					int[] list = getEntries(i);
					if (list != null) {
						for(int pp: list) {
							pageManager.release(pp);
						}
						setEntries(i, null);
					}
				}
				
				hashtable = new AtomicIntegerArray(1024);
				capacity = hashtable.length() >> 1;
				size.set(0);
			}
			finally {
				tableLock.writeLock().unlock();
			}
		}

		private boolean sameKey(ByteChunk entry, ByteChunk key) {
			int keySize = entry.intAt(0);
			if (keySize == key.lenght()) {
				for (int i = 0; i != keySize; ++i) {
					if (entry.at(8 + i) != key.at(i)) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		private ByteChunk getValue(ByteChunk entry) {
			int keySize = entry.intAt(0);
			int valueSize = entry.intAt(4);
			return entry.subChunk(8 + keySize, valueSize);
		}

		@Override
		public int size() {
			return size.get();
		}

		@Override
		public ByteChunk get(ByteChunk key) {
			tableLock.readLock().lock();
			try {				
				int index = hashIndex(key, capacity);
				readLock(index);
				try {
					int[] entries = getEntries(index);
					if (entries != null) {
						for(int pp : entries) {
							ByteChunk entry = pageManager.get(pp);
							if (sameKey(entry, key)) {
								return getValue(entry);
							}
						}
					}
					return null;
				}
				finally {
					readUnlock(index);
				}
			}
			finally {
				tableLock.readLock().unlock();
			}
			
		}

		@Override
		public void put(ByteChunk key, ByteChunk value) {
			tableLock.readLock().lock();
			try {
				internalPut(key, value);
			}
			finally {
				tableLock.readLock().unlock();
			}
			checkTableSize();
		}

		@Override
		public void remove(ByteChunk key) {
			tableLock.readLock().lock();
			try {
				int index = hashIndex(key, capacity);
				writeLock(index);
				try {
					
					int[] entries = getEntries(index);
					
					if (entries != null) {
						for(int pp : entries) {
							ByteChunk entry = pageManager.get(pp);
							if (sameKey(entry, key)) {
								pageManager.release(pp);
								if (entries.length == 1) {
									setEntries(index, null);
								}
								else {
									int[] newEntries = new int[entries.length - 1];
									int n = 0;
									for(int pi :  entries) {
										if (pi != pp) {
											newEntries[n++] = pi;
										}
									}
									setEntries(index, newEntries);
								}
								size.decrementAndGet();
								return;
							}
						}
					}
				}
				finally {
					writeUnlock(index);
				}
			}
			finally {
				tableLock.readLock().unlock();
			}
			checkTableSize();
		}

		// table lock is assumed
		private void internalPut(ByteChunk key, ByteChunk value) {

			int index = hashIndex(key, capacity);
			writeLock(index);
			try {			
				int[] entries = getEntries(index);
				
				if (entries != null) {
					for(int i = 0; i != entries.length; ++i) {
						int pp = entries[i];
						ByteChunk entry = pageManager.get(pp);
						if (sameKey(entry, key)) {
							// overriding value
							pageManager.release(pp);
							int npp = pageManager.allocate(8 + key.lenght() + value.lenght(), ALLOC_NEW_VALUE);
							ByteChunk chunk = pageManager.get(npp);
							chunk.putInt(0, key.lenght());
							chunk.putInt(4, value.lenght());
							chunk.putBytes(8, key);
							chunk.putBytes(8 + key.lenght(), value);
							entries[i] = npp;
							setEntries(index, entries);
							return;
						}
					}
				}
					
				// add new entry
				int npp = pageManager.allocate(8 + key.lenght() + value.lenght(), ALLOC_NEW_VALUE);
				ByteChunk chunk = pageManager.get(npp);
				chunk.putInt(0, key.lenght());
				chunk.putInt(4, value.lenght());
				chunk.putBytes(8, key);
				chunk.putBytes(8 + key.lenght(), value);

				int[] newEntries;
				if (entries == null || entries.length == 0) {
					newEntries = new int[]{npp};
				}
				else {
					newEntries = Arrays.copyOf(entries, entries.length + 1);
					newEntries[entries.length] = npp;
				}
				
				setEntries(index, newEntries);
				
				size.incrementAndGet();
			}
			finally {
				writeUnlock(index);
			}
		}
		
		// tableLock assumed
		void recycleEntry(int index) {
			writeLock(index);
			try {
				if (index >= capacity) {
					return;
				}
				
				int[] entries = getEntries(index);
				
				if (entries != null && entries.length > 0) {
					boolean modified = false;
					for(int i = 0; i != entries.length; ++i) {
						int pp = entries[i];
						if (needRecycle(pp)) {
							ByteChunk chunk = pageManager.get(pp);
							int npp = pageManager.allocate(chunk.lenght(), ALLOC_RELOCATE_VALUE);
							ByteChunk newChunk = pageManager.get(npp);
							newChunk.putBytes(chunk);
							pageManager.release(pp);
							entries[i] = npp;
							modified = true;
						}
					}
					
					if (!modified) {
						int pe = hashtable.get(index);
						pe = pe > 0 ? pe : -pe;
						if (needRecycle(pe)) {
							modified = true;
						}
					}
					
					if (modified) {
						setEntries(index, entries);
					}
				}
			}
			finally {
				writeUnlock(index);
			}
		}
		
		private boolean needRecycle(int pointer) {
			return pageManager.isMarkedForRecycle(pointer);
		}

		private void checkTableSize() {			
			float loadFactor = ((float)size.get()) / capacity;
			if (loadFactor > thresholdLoadFactor && (capacity == hashtable.length())){
				checkTablePhysicalSize();
			}
			if (loadFactor > targetLoadFactor && (capacity < hashtable.length())) {				
				growTable(4);
			}
		}
		
	    void checkTablePhysicalSize() {
	    	if (capacity > ((hashtable.length() * 8) / 10)) {
	    		// need to resize table
	    		int delta = (hashtable.length() / 2) & 0xFFFFF800;
	    		if (delta < 1024) {
	    			delta = 1024;
	    		}
	    		
	    		tableLock.writeLock().lock();
	    		try {
	    			AtomicIntegerArray newTable = new AtomicIntegerArray(hashtable.length() + delta);
	    			for(int i = 0; i != hashtable.length(); ++i) {
	    				newTable.set(i, hashtable.get(i));
	    			}
	    			hashtable = newTable;
	    			locktable = createLocktable(hashtable.length());
	    		}
	    		finally {
	    			tableLock.writeLock().unlock();
	    		}
	    	}
		}

		private void growTable(int n) {
			tableLock.readLock().lock();
            //checkHashConsistency();
			try {
	            for(int i = 0; i != n; ++i) {
	            	if (capacity == hashtable.length()) {
	            		return;
	            	}
	            	int nRound = Integer.highestOneBit(capacity);
	            	int nSplit = (capacity) & ~nRound;
	            	int nLast = capacity;
	            	writeLock(nSplit);
	            	writeLock(nLast);
	            	try {
		                ++capacity;
		                int[] entries = getEntries(nSplit);
		                if (entries != null) {
		                	int n1 = 0;
		                	int[] el1 = new int[entries.length];
		                	int n2 = 0;
		                	int[] el2 = new int[entries.length];
		                	
		                	for(int pp: entries) {
		                		ByteChunk chunk = pageManager.get(pp);
		        				int keySize = chunk.intAt(0);
		        				ByteChunk key = chunk.subChunk(8, keySize);
		                		int index = hashIndex(key, capacity);
		                		if (index == nSplit) {
		                			el1[n1++] = pp;
		                		}
		                		else if (index == nLast) {
		                			el2[n2++] = pp;
		                		}
		                		else {
		                			throw new AssertionError();
		                		}
		                	}
		                	el1 = Arrays.copyOf(el1, n1);
		                	el2 = Arrays.copyOf(el2, n2);
		                	
		                	setEntries(nSplit, el1);
		                	setEntries(nLast, el2);
		                }
	            	}
	            	finally {
	            		writeUnlock(nSplit);
	            		writeUnlock(nLast);
	            	}
		            //checkHashConsistency();
	            }
			}
            finally {
            	tableLock.readLock().unlock();
            }
	    }

		@SuppressWarnings("unused") // for testing
	    private void checkHashConsistency() {
	        tableLock.readLock().lock();
	        try {
	            for(int i = 0; i != capacity; ++i) {
	            	int[] entries = getEntries(i);
	            	if (entries != null) {
		            	for(int pp : entries) {
		            		ByteChunk entry = pageManager.get(pp);
		            		int keySize = entry.intAt(0);
		            		ByteChunk key = entry.subChunk(8, keySize);
		            		if (hashIndex(key, capacity) != i) {
		            			throw new AssertionError();
		            		}
		            	}
	            	}
	            }            
	        }
	        finally {
	            tableLock.readLock().unlock();
	        }
	    }
	    
		private int hashIndex(ByteChunk key, int capacity) {
	        int hash = BinHash.hash(key);
	        return splitHash(hash, capacity);
	    }
		
		public AtomicIntegerArray createLocktable(int size) {
			AtomicIntegerArray table = new AtomicIntegerArray(size / 4); // 8 bits per lock
			return table;
		}
		
		private void readLock(int index) {
			int n = 0;
			while(true) {
				byte c = byte_get(locktable, index);
				if (c >= 0 && c < 126) {
					byte u = (byte) (c + 1) ;
					if (byte_compareAndSet(locktable, index, c, u)) {
						return;
					}
				}				
				++n;
				if (n % 10 == 0) {
					Thread.yield();
				}
			}
		}

		private void readUnlock(int index) {
			int n = 0;
			while(true) {
				byte c = byte_get(locktable, index);
				if (c > 0) {
					byte u = (byte) (c - 1) ;
					if (byte_compareAndSet(locktable, index, c, u)) {
						return;
					}
				}				
				else if (c < 0) {
					byte u = (byte) (c + 1);
					if (byte_compareAndSet(locktable, index, c, u)) {
						return;
					}
				}
				else {
					throw new IllegalStateException("Invalid lock state");
				}
				++n;
				if (n % 10 == 0) {
					Thread.yield();
				}
			}
		}
		
		private void writeLock(int index) {
			int n = 0;
			while(true) {
				byte c = byte_get(locktable, index);
				if (c == 0) {
					byte u = (byte) -1;
					if (byte_compareAndSet(locktable, index, c, u)) {
						return;
					}
				}				
				else if (c < 0) {
					// another writer is pending					
				}
				else if (c > 0){
					byte u = (byte) (-c - 1);
					if (byte_compareAndSet(locktable, index, c, u)) {
						break;
					}
				}
				++n;
				if (n % 10 == 0) {
					Thread.yield();
				}
			}
			// waiting read locks to get released
			while(true) {
				byte c = byte_get(locktable, index);
				if (c == -1) {
					return;
				}				

				++n;
				if (n % 10 == 0) {
					Thread.yield();
				}				
			}			
		}

		private void writeUnlock(int index) {
			int n = 0;
			while(true) {
				byte c = byte_get(locktable, index);
				if (c == -1) {
					byte u = (byte) 0;
					if (byte_compareAndSet(locktable, index, c, u)) {
						return;
					}
				}				
				else {
					throw new IllegalStateException("Broken lock");
				}
				++n;
				if (n % 10 == 0) {
					Thread.yield();
				}
			}
		}
		
		private byte byte_get(AtomicIntegerArray table, int index) {
			int x = index / 4;
			int xx = index % 4;			
			int word = table.get(x);
			return getByte(word, xx);
		}

		private boolean byte_compareAndSet(AtomicIntegerArray table, int index, byte expected, byte newValue) {
			int x = index / 4;
			int xx = index % 4;
			
			while(true) {
				int word = table.get(x);
				byte val = getByte(word, xx);
				if (val == expected) {
					int newWord = setByte(word, xx, newValue);
					if (table.compareAndSet(x, word, newWord)) {
						return true;
					}
					else {
						continue;
					}
				}
				else {
					return false;
				}				
			}			
		}
		
		private byte getByte(int word, int i) {
			switch(i) {
			case 0:
				return (byte) (0xFF & word);
			case 1:
				return (byte) (0xFF & (word >> 8));				
			case 2:
				return (byte) (0xFF & (word >> 16));				
			case 3:
				return (byte) (0xFF & (word >> 24));				
			default:
				throw new IllegalArgumentException("4 bytes per int");
			}			
		}
		
		private int setByte(int word,int i, byte value) {
			switch(i) {
			case 0:
				word &= 0xFFFFFF00;
				word |= 0xFF & (int)value;
				return word;
			case 1:
				word &= 0xFFFF00FF;
				word |= (0xFF & (int)value) << 8;
				return word;				
			case 2:
				word &= 0xFF00FFFF;
				word |= (0xFF & (int)value) << 16;
				return word;				
			case 3:
				word &= 0x00FFFFFF;
				word |= (0xFF & (int)value) << 24;
				return word;				
			default:
				throw new IllegalArgumentException("4 bytes per int");
			}
		}
		
		@Override
		public Iterator<ByteChunk> keys() {
			return null;
		}
	}

    static int splitHash(int hash, int capacity) {
        int round = Integer.highestOneBit(capacity);
        int split = capacity & ~round;

        long idx = (0xFFFFFFFFl & hash) % (round);
        
        if (idx < split) {
        	idx = (0xFFFFFFFFl & hash) % (round << 1);
        }
        return (int) idx;
	}
	
	private class HashIterator implements Iterator<ByteChunk> {
	
		private final AtomicLongArray hashtable;
		private int position = 0;
		private final List<ByteChunk> buffer = new ArrayList<ByteChunk>();
		
		public HashIterator(AtomicLongArray hashtable) {
			this.hashtable = hashtable;
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ByteChunk next() {
			if (hasNext()) { 
				return buffer.remove(0);
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}
}
