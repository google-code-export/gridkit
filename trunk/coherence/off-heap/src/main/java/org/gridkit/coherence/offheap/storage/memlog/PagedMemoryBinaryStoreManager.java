package org.gridkit.coherence.offheap.storage.memlog;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PagedMemoryBinaryStoreManager implements BinaryStoreManager {

	private static long EMPTY = 0;
	private static long LOCKED = 1;
	
	private static long TABLE_SERVICE_PERIOD = TimeUnit.SECONDS.toNanos(30);
	private static long MEM_DIAG_REPORT_PERIOD = TimeUnit.SECONDS.toNanos(30);
	
	private final String name;
	private List<BinaryHashTable> tables = new ArrayList<BinaryHashTable>();
	private PageLogManager pageManager;
	private Thread maintenanceDaemon;
	
	public PagedMemoryBinaryStoreManager(String name, PageLogManager pageManager) {
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

		AtomicLongArray hashtable = new AtomicLongArray(1024);
		int capacity = hashtable.length() >> 1;
		ReadWriteLock tableLock = new ReentrantReadWriteLock();

		AtomicInteger size = new AtomicInteger();
		float targetLoadFactor = 0.5f;
		
		long maintenanceTimestamp = System.nanoTime();
					
		private long entryPointer(AtomicLongArray table, int index) {
			int i = 0;
			while(true) {
				long pointer = table.get(index);
				if (pointer == LOCKED) {
					i++;
					if (i % 100 == 0) {
						Thread.yield();
					}
					continue;
				}
				return pointer;
			}
		}

		public void clear() {
			tableLock.writeLock().lock();
			try {
			
				for(int i = 0; i != capacity; ++i) {
					long pp = hashtable.get(i);
					if (pp != EMPTY) {
						wipe(pp);
						hashtable.set(i, 0);
					}
				}
				
				hashtable = new AtomicLongArray(1024);
				capacity = hashtable.length() >> 1;
				size.set(0);
			}
			finally {
				tableLock.writeLock().unlock();
			}
		}

		private long lockEntry(AtomicLongArray table, int index) {
			int i = 0;
			while(true) {
				long pointer = table.getAndSet(index, LOCKED);
				if (pointer == LOCKED) {
					i++;
					if (i % 100 == 0) {
						Thread.yield();
					}
					continue;
				}
				return pointer;
			}
		}

		private void unlockEntry(AtomicLongArray table, int index, long pointer) {
			// should always work
			table.compareAndSet(index, LOCKED, pointer);
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

		private long nextEntry(ByteChunk entry) {
			int keySize = entry.intAt(0);
			int valueSize = entry.intAt(4);
			long next = (entry.lenght() > (8 + keySize + valueSize)) ? entry.longAt(entry.lenght() - 8) : EMPTY;
			return next;
		}
		
		private void setNextEntry(ByteChunk chunk, long next) {
			if (next != EMPTY) {
				pageManager.validate(next);
			}
//			if ((next >> 32) > 0) {
//				new String();
//			}
			int keySize = chunk.intAt(0);
			int valueSize = chunk.intAt(4);
			if (chunk.lenght() == (8 + keySize + valueSize)) {
				throw new IllegalArgumentException();
			}
			
			chunk.putLong(chunk.lenght() - 8, next);
		}
		
		@Override
		public ByteChunk get(ByteChunk key) {
			AtomicLongArray table;
			int cap;
			tableLock.readLock().lock();
			try {
				table = hashtable;
				cap = capacity;
			}
			finally {
				tableLock.readLock().unlock();
			}
			
			int index = hashIndex(key, cap);
			long entry = entryPointer(table, index);
			
			if (entry == EMPTY) {
				return null;
			}
			else {
				while(true) {
					ByteChunk chunk = pageManager.get(entry);
					if (sameKey(chunk, key)) {
						ByteChunk result = getValue(chunk);
						return result;
					}
					else {
						entry = nextEntry(chunk);
						if (entry == EMPTY) {
							return null;
						}
					}
				}
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

		private void internalPut(ByteChunk key, ByteChunk value) {
			AtomicLongArray table;
			int cap;
			table = hashtable;
			cap = capacity;

			int index = hashIndex(key, cap);
			long entry = lockEntry(table, index);
			long cleanUp = entry;
			
			try {
				if (entry == EMPTY) {
					// fast path - single entry
					entry = pageManager.allocate(8 + key.lenght() + value.lenght());
					ByteChunk chunk = pageManager.get(entry);
					chunk.putInt(0, key.lenght());
					chunk.putInt(4, value.lenght());
					chunk.putBytes(8, key);
					chunk.putBytes(8 + key.lenght(), value);
					// update table entry on finalize
					
					size.incrementAndGet();
				}
				else {
					
					boolean override = false;

					long oldEntry = entry;
					long newEntry = pageManager.allocate(16 + key.lenght() + value.lenght());
					ByteChunk chunk = pageManager.get(newEntry);
					chunk.putInt(0, key.lenght());
					chunk.putInt(4, value.lenght());
					chunk.putBytes(8, key);
					chunk.putBytes(8 + key.lenght(), value);
					
					while(oldEntry != 0) {
						ByteChunk oldChunk = pageManager.get(oldEntry);
						if (sameKey(oldChunk, key)) {
							override = true;
						}
						else {
							long pp = pageManager.allocate(oldChunk.lenght());
							ByteChunk clone = pageManager.get(pp);
							clone.putBytes(0, oldChunk);
							if (nextEntry(clone) != EMPTY) {
								setNextEntry(clone, EMPTY);
							}
							setNextEntry(chunk, pp);
							chunk = clone;
						}
						
						oldEntry = nextEntry(oldChunk); 
					}
					
					entry = newEntry;
					// update table entry on finalize

					if (!override) {
						size.incrementAndGet();
					}
				}
			}
			finally {
				unlockEntry(table, index, entry);
				if (cleanUp != entry) {
					wipe(cleanUp);
				}
			}
		}
		
		void recycleEntry(int index) {
			tableLock.readLock().lock();
			try {
				if (index > capacity) {
					return;
				}
				
				long pointer = hashtable.get(index);
				if (needRecycle(pointer)) {
					long newPointer = clone(pointer);
					if (hashtable.compareAndSet(index, pointer, newPointer)) {
						wipe(pointer);
					}
					else {
						wipe(newPointer);
					}
				}
			}
			finally {
				tableLock.readLock().unlock();
			}
		}
		
		private boolean needRecycle(long pointer) {
			while(pointer != EMPTY) {
				if (pageManager.isMarkedForRecycle(pointer)) {
					return true;
				}
				else {
					ByteChunk chunk = pageManager.get(pointer);
					pointer = nextEntry(chunk);
				}
			}
			return false;
		}

		private long clone(long pointer) {
			ByteChunk source = pageManager.get(pointer);
			long pp = pageManager.allocate(source.lenght());
			ByteChunk target = pageManager.get(pp);
			target.putBytes(source);
			
			pointer = nextEntry(source);
			while(pointer != EMPTY) {
				source = pageManager.get(pointer);
				long next = pageManager.allocate(source.lenght());
				ByteChunk nextChunk = pageManager.get(next);
				nextChunk.putBytes(source);
				setNextEntry(target, next);
				pointer = nextEntry(source);
				target = nextChunk;
			}
			
			return pp;
		}		
		
		private void wipe(long pointer) {
			while(pointer != EMPTY) {
				ByteChunk chunk = pageManager.get(pointer);
				pageManager.release(pointer);
				pointer = nextEntry(chunk);
			}
		}

		private void checkTableSize() {			
			float loadFactor = ((float)size.get()) / capacity;
			if (loadFactor > targetLoadFactor && (capacity < hashtable.length())) {
				growTable(32);
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
	    			AtomicLongArray newTable = new AtomicLongArray(hashtable.length() + delta);
	    			for(int i = 0; i != hashtable.length(); ++i) {
	    				newTable.set(i, hashtable.get(i));
	    			}
	    			hashtable = newTable;
	    		}
	    		finally {
	    			tableLock.writeLock().unlock();
	    		}
	    	}
		}

		public void growTable(int n) {
	        // TODO reimplement using read lock only
            /*checkHashConsistency();*/
            for(int i = 0; i != n; ++i) {
            	tableLock.writeLock().lock();
            	if (capacity == hashtable.length()) {
            		return;
            	}
            	int nRound = Integer.highestOneBit(capacity);
            	int nSplit = (capacity) & ~nRound;
            	long pointer = hashtable.get(nSplit);
            	try {
	                ++capacity;
	                if (pointer != EMPTY) {
	                	rehash(pointer, nSplit);
	                }
            	}
	            finally {
	            	tableLock.writeLock().unlock();
	            }
	            /*checkHashConsistency();*/
            }
	    }

	    /* run under write lock */
	    /* TODO avoid global lock, by fine grained locking */
	    private void rehash(long pointer, int index) {
	    	if (!hashtable.compareAndSet(index, pointer, EMPTY)) {
	    		throw new AssertionError();
	    	}
	    	long root = pointer;
			while(pointer != EMPTY) {
				ByteChunk chunk = pageManager.get(pointer);
				int keySize = chunk.intAt(0);
				int valueSize = chunk.intAt(4);
				ByteChunk key = chunk.subChunk(8, keySize);
				ByteChunk value = chunk.subChunk(8 + keySize, valueSize);
				internalPut(key, value);
				pointer = nextEntry(chunk);
			}
			wipe(root);
		}

		@SuppressWarnings("unused") // for testing
	    private void checkHashConsistency() {
	        tableLock.readLock().lock();
	        try {
	            for(int i = 0; i != capacity; ++i) {
	            	long pointer = entryPointer(hashtable, i);
	            	while(pointer != EMPTY) {
	            		ByteChunk entry = pageManager.get(pointer);
	            		int keySize = entry.intAt(0);
	            		ByteChunk key = entry.subChunk(8, keySize);
                        if (hashIndex(key, capacity) != i) {
                            throw new AssertionError();
                        }
	            	}
	            }            
	        }
	        finally {
	            tableLock.readLock().unlock();
	        }
	    }
	    
		@Override
		public void remove(ByteChunk key) {
			AtomicLongArray table;
			int cap;
			tableLock.readLock().lock();
			try {
				table = hashtable;
				cap = capacity;
			
				int index = hashIndex(key, cap);
				long entry = lockEntry(table, index);
				long cleanUp = entry;
				
				try {
					if (entry == EMPTY) {
						// nothing
						return;
					}
					else {
						
						boolean removed = false;

						long oldEntry = entry;
						long newEntry = EMPTY;
						ByteChunk chunk = null;
						
						while(oldEntry != 0) {
							ByteChunk oldChunk = pageManager.get(oldEntry);
							if (sameKey(oldChunk, key)) {
								removed = true;
							}
							else {
								long pp = pageManager.allocate(oldChunk.lenght());
								ByteChunk clone = pageManager.get(pp);
								clone.putBytes(0, oldChunk);
								if (nextEntry(clone) != EMPTY) {
									setNextEntry(clone, 0);
								}
								if (chunk != null) {
									setNextEntry(chunk, pp);
								}
								else {
									newEntry = pp;
								}
								chunk = clone;
							}
							
							oldEntry = nextEntry(oldChunk); 
						}
						
						entry = newEntry;
						// update table entry on finalize

						if (removed) {
							size.decrementAndGet();
						}
					}
				}
				finally {
					unlockEntry(table, index, entry);
					if (cleanUp != entry) {
						wipe(cleanUp);
					}
				}
			}
			finally {
				tableLock.readLock().unlock();
			}
			checkTableSize();
		}
		
		private int hashIndex(ByteChunk key, int capacity) {
	        int hash = BinHash.hash(key);
	        return splitHash(hash, capacity);
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
