package org.gridkit.coherence.offheap.storage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class PagedLinearHashStore<S> implements ObjectStore {

    protected final DynamicAtomicArray<S> table;
    // TODO is it possible to avoid lock?
    protected ReentrantReadWriteLock tableLock = new ReentrantReadWriteLock();
    private volatile int tableCapacity; 

    protected final int targetPageSize;
    protected final int targetPageSizePower;
    
    protected AtomicLong size = new AtomicLong();
    protected AtomicInteger pageCount = new AtomicInteger();


    public PagedLinearHashStore(DynamicAtomicArray<S> table, int initialCapacity, int targetPageSize) {
        this.table = table;
        this.tableCapacity = initialCapacity;
        this.table.setLength(initialCapacity);
        this.targetPageSize = targetPageSize;
        this.targetPageSizePower = getRawPower(targetPageSize);
    }
    
    private int getRawPower(int val) {
        for (int i = 0; i < 31; ++i) {
            if (val <= (1 << i)) {
                return i;
            }
        }
        return 31;
    }

    public int size() {
        return size.intValue();
    }
    
    @Override
    public Object load(Object key) {
        tableLock.readLock().lock();
        try {
            S slot = getHashSlot(hashIndex(key), false);
            if (slot != null) {
                return slotGet(slot, key);
            }
            else {
                return null;
            }
        }
        finally {
            tableLock.readLock().unlock();
        }
    }

    @Override
    public void store(Object key, Object value) {
        tableLock.readLock().lock();
        try {
            S slot = getHashSlot(hashIndex(key), true);
            if (slotPut(slot, key, value)) {
                size.incrementAndGet();
            }
        }
        finally {
            tableLock.readLock().unlock();
        }
        
        checkSLA();
    }

    @Override
    public void erase(Object key) {
        tableLock.readLock().lock();
        try {
            S slot = getHashSlot(hashIndex(key), false);
            if (slot != null && slotErase(slot, key)) {
                size.decrementAndGet();
            }
        }
        finally {
            tableLock.readLock().unlock();
        }
        
        checkSLA();
    }
    
    @Override
    public void eraseAll() {
        tableLock.readLock().lock();
        try {
            for(int i = 0; i < tableCapacity; ++i) {
                S slot = table.getAndSet(i, null);
                if (slot != null) {
                    size.addAndGet(-slotDestroy(slot));
                    pageCount.decrementAndGet();
                }
            }
        }
        finally {
            tableLock.readLock().unlock();
        }
    }
    
    // TODO get hash slot locked
    private S getHashSlot(int hashIndex, boolean create) {
        S slot = table.get(hashIndex);
        if (create && slot == null) {
            S newSlot = slotCreate(hashIndex);
            table.compareAndSet(hashIndex, null, newSlot);
            slot = table.get(hashIndex);
            if (newSlot == slot) {
                pageCount.incrementAndGet();
            }
            else {
                slotDestroy(newSlot);
            }
        }
        
        return slot;
    }

    protected void checkSLA() {
        // do nothing
    }
    
    protected abstract Object slotGet(S slot, Object key);

    protected abstract Iterator<Object> slotKeys(S slot);
    
    /**
     * @return <code>true</code> if insert, <code>false</code> if replace
     */
    protected abstract boolean slotPut(S slot, Object key, Object value);

    /**
     * @return <code>true</code> if removed, <code>false</code> if no such element
     */
    protected abstract boolean slotErase(S slot, Object key);
    
    /**
     * Place provide map as content of slot, removing current values
     */
    protected abstract void slotImport(S slot, Map<Object, Object> data);

    /**
     * @return content of slot
     */
    protected abstract Map<Object, Object> slotExport(S slot);

    protected abstract int size(S slot);
    
    protected abstract S slotCreate(int index);
    /**
     * @return size of slot at the moment of destruction
     */
    protected abstract int slotDestroy(S slot);

    public int getTargetPageSize() {
        return targetPageSize;
    }
    
    public int getAveragePageSize() {
        float avg = ((float)size.intValue()) / tableCapacity; //pageCount.intValue(); - is more fair but may lead to some issues with bad hash
        return (int) avg;
    }

    public int getTableSize() {
        return tableCapacity;
    }
    
    @SuppressWarnings("unused") // for testing
    private void checkHashConsistency() {
        tableLock.readLock().lock();
        try {
            for(int i = 0; i != tableCapacity; ++i) {
                S slot = getHashSlot(i, false);
                if (slot != null) {
                    for(Object key: slotExport(slot).keySet()) {
                        if (hashIndex(key) != i) {
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
    
    public void growTable(int n) {
        
        table.setLength(tableCapacity + n);
        
        tableLock.writeLock().lock();
        try {
        
            /*checkHashConsistency();*/
            for(int i = 0; i != n; ++i) {
                int nRound = Integer.highestOneBit(tableCapacity);
                int nSplit = (tableCapacity) & ~nRound;
                ++tableCapacity;
                S slot = table.get(nSplit);
                if (slot != null) {
                    rehash(slot, nSplit, ++tableCapacity);
                }
            }
            /*checkHashConsistency();*/
        }
        finally {
            tableLock.writeLock().unlock();
        }
    }
    
//    protected abstract void rehashUnderLock(S slot1, int index1, S slot2, int index2, int newCapacity);
//    
//    protected void lockedRehash(S slot1, int index1, S slot2, int index2, int newCapacity) {
//        
//    }

    private void rehash(S slot, int index, int capacity) {
        Map<Object, Object> content = slotExport(slot);
        Map<Object, Object> moved = new HashMap<Object, Object>();
        int nextIdx = index;
        
        for(Map.Entry<Object, Object> entry: content.entrySet()) {
            Object key = entry.getKey();
            int newIdx = hashIndex(key, capacity);
            if (newIdx != index) {
                nextIdx = newIdx;
                moved.put(entry.getKey(), entry.getValue());
            }
        }
        
        if (!moved.isEmpty()) {
            content.keySet().removeAll(moved.keySet());
            slotImport(slot, content);
            if (getHashSlot(nextIdx, false) != null) {
                throw new AssertionError("Broken hash");
            }
            S newSlot = getHashSlot(nextIdx, true);
            slotImport(newSlot, moved);
        }
    }

    private int hashIndex(Object key) {
        int hash = hash(key);
        return splitHash(hash, tableCapacity);
    }

    private int hashIndex(Object key, int capacity) {
        int hash = hash(key);
        return splitHash(hash, capacity);
    }
    
    private static int splitHash(int hash, int capacity) {
        int round = Integer.highestOneBit(capacity);
        int split = capacity & ~round;

        int idx = hash % (round);
        
        if (idx >= split) {
            return idx;
        } else {
            return hash % (round << 1);
        }
    }

    @Override
    public Iterator<Object> keys() {
        return new KeyIterator();
    }

    private int hash(Object key) {
        int h = key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return (h ^ (h >>> 7) ^ (h >>> 4)) >>> targetPageSizePower;
    }

    class KeyIterator implements Iterator<Object> {
        private int nextSlot;
        private Iterator<Object> segment;

        private void nextSegment() {
            while (nextSlot < tableCapacity) {
                S slot = table.get(nextSlot++);
                if (slot != null) {
                    segment = slotKeys(slot);
                    break;
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (segment == null || !segment.hasNext()) {
                nextSegment();
            }
            return segment != null && segment.hasNext();
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return segment.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
