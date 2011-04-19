/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class SegmentedDynamicAtomicArray<E> implements DynamicAtomicArray<E> {

    private volatile AtomicReferenceArray<E>[] segments;
    
    private final int segmentSizePower;
    private volatile int size;

    public SegmentedDynamicAtomicArray(int segmentSize) {
        segmentSizePower = getPower(segmentSize);
        segments = newSegments(0);
        size = 0;
    }

    @SuppressWarnings("unchecked")
    private AtomicReferenceArray<E>[] newSegments(int i) {
        return new AtomicReferenceArray[i];
    }

    private int getPower(int val) {
        // TODO binary search
        for (int i = 0; i < 31; ++i) {
            if (val == 1 << i) {
                return i;
            }
        }
        throw new IllegalArgumentException("Super page size should be a power of 2");
    }
    
    protected synchronized void resizeTable(int pageCount) {
        int segmentSize = 1 << segmentSizePower;
        AtomicReferenceArray<E>[] newSegments = newSegments(pageCount);
        if (segments != null) {
           System.arraycopy(segments, 0, newSegments, 0, Math.min(segments.length, newSegments.length)); 
        }
        for(int i = 0; i < newSegments.length; ++i) {
            if (newSegments[i] == null) {
                newSegments[i] = new AtomicReferenceArray<E>(segmentSize);
            }
        }
        
        segments = newSegments;
        // after this point array assigned to tableSegments will never change,
        // so we can avoid synchronization, using volatile modifier instead
    }
    
    @Override
    public int length() {
        return size;
    }
    
    @Override
    public synchronized void setLength(int length) {
        if (length == size) {
            return;
        }
        if (length < size) {
            for(int i = length; i != size; ++i) {
                set(i, null);
            }
         // should do before actual resize
            size = length;
        }
        int pages = (length + ((1 << segmentSizePower) - 1)) >> segmentSizePower;
        if (pages != segments.length) {
            resizeTable(pages);
        }
        if (length > size) {
            // should do after actual resize
            size = length;
        }
    }

    @Override
    public boolean compareAndSet(int i, E expect, E update) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        return segments[supIdx].compareAndSet(subIdx, expect, update);
    }

    @Override
    public E get(int i) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        return segments[supIdx].get(subIdx);
    }

    @Override
    public E getAndSet(int i, E newValue) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        return segments[supIdx].getAndSet(subIdx, newValue);
    }

    @Override
    public void lazySet(int i, E newValue) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        segments[supIdx].lazySet(subIdx, newValue);    
    }

    @Override
    public void set(int i, E newValue) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        segments[supIdx].set(subIdx, newValue);
    }

    @Override
    public boolean weakCompareAndSet(int i, E expect, E update) {
        if (i >= size) {
            throw new ArrayIndexOutOfBoundsException(i);
        }
        int supIdx = i >> segmentSizePower;
        int subIdx = i & ((1 << segmentSizePower) - 1);
        return segments[supIdx].weakCompareAndSet(subIdx, expect, update);
    }
}
