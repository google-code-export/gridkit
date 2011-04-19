/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import com.tangosol.io.BinaryStore;
import com.tangosol.util.Binary;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class BinaryPackedBytesHashStore extends PackedBytesHashStore implements BinaryStore {

    public BinaryPackedBytesHashStore(int initialCapacity, int targetPageSize, boolean autoResize) {
        super(new SegmentedDynamicAtomicArray<Page>(256), initialCapacity, targetPageSize, autoResize);
    }

    public BinaryPackedBytesHashStore(int initialCapacity, int targetPageSize, int segmentSize, boolean rehashDaemon) {
        super(new SegmentedDynamicAtomicArray<Page>(segmentSize), initialCapacity, targetPageSize, false);
        if (rehashDaemon) {
            new RehashDaemon(this, 10).start();
        }
    }

    @Override
    public void erase(Binary binKey) {
        erase((Object)binKey);        
    }

    @Override
    public Binary load(Binary binKey) {
        return (Binary)load((Object)binKey);
    }

    @Override
    public void store(Binary binKey, Binary binValue) {
        store((Object)binKey, (Object)binValue);        
    }

    @Override
    protected Object keyFromBytes(byte[] data, int offs, int size) {
        return new Binary(data, offs, size);
    }

    @Override
    protected Object valueFromBytes(byte[] data, int offs, int size) {
        return new Binary(data, offs, size);
    }
    
    @Override
    protected byte[] keyToBytes(Object key) {
        return ((Binary)key).toByteArray();
    }

    @Override
    protected byte[] valueToBytes(Object value) {
        return ((Binary)value).toByteArray();
    }
}
