/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

//import junit.framework.AssertionFailedError;

/**
 * Simple compact store. Objects in page are stored as byte array, sorted by binary keys presentation.
 *  
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public abstract class PackedBytesHashStore extends PagedLinearHashStore<PackedBytesHashStore.Page> {

    private boolean autoResize = true;
    
    public PackedBytesHashStore(DynamicAtomicArray<Page> table, int initialCapacity, int targetPageSize, boolean autoResize) {
        super(table, initialCapacity, targetPageSize);
        this.autoResize = autoResize;
    }

    @Override
    protected void checkSLA() {
        if (autoResize) {
            int targetSize = size() / targetPageSize;
            int delta = targetSize - getTableSize();
            if (delta > 128) {
                tableLock.writeLock().lock();
                try {
                    targetSize = size() / targetPageSize;
                    delta = targetSize - getTableSize();
                    if (delta > 128) {
                        growTable(delta);
                    }
                }
                finally {
                    tableLock.writeLock().unlock();
                }
            }
        }
    }



    @Override
    protected int size(Page slot) {
        synchronized(slot) {
            return slot.size();
        }
    }
    
    @Override
    protected Page slotCreate(int index) {
        return new Page();
    }
    
    @Override
    protected int slotDestroy(Page slot) {
        synchronized(slot) {
            return slot.size();
        }
    }
    
    @Override
    protected Object slotGet(Page slot, Object key) {
        synchronized(slot) {
            int idx = slot.lookup(keyToBytes(key));
            if (idx >= 0) {
                Object value = valueFromBytes(slot.data, slot.valueOffset(idx), slot.valueSize(idx));
                return value;
            }
            else {
                return null;
            }
        }
    }

    @Override
    protected Iterator<Object> slotKeys(Page slot) {
        synchronized(slot) {
            Object[] keys = new Object[slot.size()];
            for(int i = 0; i != keys.length; ++i) {
                keys[i] = keyFromBytes(slot.data, slot.keyOffset(i), slot.keySize(i));
            }
            return Arrays.asList(keys).iterator();
        }
    }

    @Override
    protected boolean slotPut(Page slot, Object key, Object value) {
        synchronized(slot) {
            Map<byte[], byte[]> map = slot.toMap(); // TODO cached weak pointer?
            byte[] old = map.put(keyToBytes(key), valueToBytes(value));
            slot.setData(map);
            return old == null;
        }
    }

    @Override
    protected boolean slotErase(Page slot, Object key) {
        synchronized(slot) {
            byte[] keyBytes = keyToBytes(key);
            int idx = slot.lookup(keyBytes);
            if (idx >= 0) {
                Map<byte[], byte[]> map = slot.toMap();
                map.remove(keyBytes);
                slot.setData(map);
                return true;
            }
            return false;
        }
    }
    
    @Override
    protected void slotImport(Page slot, Map<Object, Object> data) {
        synchronized(slot) {
            TreeMap<byte[], byte[]> bin = new TreeMap<byte[], byte[]>(ByteArrayComparator.INSTANCE);
            for(Map.Entry<Object, Object> entry: data.entrySet()) {
                bin.put(keyToBytes(entry.getKey()), valueToBytes(entry.getValue()));
            }
            slot.setData(bin);
        };
    }
    
    @Override
    protected Map<Object, Object> slotExport(Page slot) {
        synchronized(slot) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            int size = slot.size();
            for(int i = 0; i != size; ++i) {
                map.put(keyFromBytes(slot.data, slot.keyOffset(i), slot.keySize(i)), valueFromBytes(slot.data, slot.valueOffset(i), slot.valueSize(i)));
            }
            return map;
        }
    }

    protected abstract byte[] keyToBytes(Object key);
    protected abstract byte[] valueToBytes(Object value);
    protected abstract Object keyFromBytes(byte[] data, int offs, int size);
    protected abstract Object valueFromBytes(byte[] data, int offs, int size);
    
    static class Page {
        
        int[] offsets;
        byte[] data;
        
        public Page() {           
            offsets = new int[0];
            data = new byte[0];
        }
        
        public void reset() {
            offsets = new int[0];
            data = new byte[0];
        }

        void setData(Map<byte[], byte[]> data) {
            pack(data);
        }
        
        int size() {
            return offsets.length >> 1;
        }
        
        int keyOffset(int keyIdx) {
            return offset(2 * keyIdx);
        }

        int keySize(int keyIdx) {
            return size(2 * keyIdx);
        }

        byte[] keyExtract(int keyIdx) {
            return extract(2 * keyIdx);
        }

        int valueOffset(int valueIdx) {
            return offset(2 * valueIdx + 1);
        }
        
        int valueSize(int valueIdx) {
            return size(2 * valueIdx + 1);
        }

        byte[] valueExtract(int valueIdx) {
            return extract(2 * valueIdx + 1);
        }
        
        
        int offset(int idx) {
            return offsets[idx];
        }

        int size(int idx) {
            return (idx + 1 < offsets.length ? offsets[idx + 1] : data.length) - offsets[idx];
        }
        
        int lookup(byte[] key) {
            int low = 0;
            int high = (offsets.length >> 1) - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                int cmp = compare(mid, key);

                if (cmp < 0) {
                    low = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    return mid; // key found
                }
            }
            return -(low + 1);  // key not found.   
        }        

        private int compare(int idx, byte[] key) {
            int offs = keyOffset(idx);
            int size= keySize(idx);
            
            for(int i = 0; i != Math.min(size, key.length); ++i) {
                int cmp = data[offs + i] - key[i];
                if (cmp != 0) {
                    return cmp;
                }
            }
            return size - key.length;
        }

        byte[] extract(int idx) {
            byte[] buf = new byte[size(idx)];
            copyBytes(data, offset(idx), buf);            
            return buf;
        }
        
        Map<byte[], byte[]> toMap() {
            Map<byte[], byte[]> map = new TreeMap<byte[], byte[]>(ByteArrayComparator.INSTANCE);
            int size = offsets.length / 2;
            for(int i = 0; i != size; ++i) {
                map.put(keyExtract(i), valueExtract(i));
            }
            return map;
        }
        
        private void pack(Map<byte[], byte[]> map) {
            int size = 0;
            for(Map.Entry<byte[], byte[]> entry: map.entrySet()) {
                size += entry.getKey().length;
                size += entry.getValue().length;
            }
            offsets = new int[2 * map.size()];
            data = new byte[size];
            int i = 0;
            int n = 0;
            for(Map.Entry<byte[], byte[]> entry: map.entrySet()) {
                offsets[i++] = n;
                copyBytes(entry.getKey(), data, n);
                n += entry.getKey().length;
                offsets[i++] = n;
                copyBytes(entry.getValue(), data, n);
                n += entry.getValue().length;
            }
            
            if (n != size) {
                throw new RuntimeException("Assetion failed");
            }
        }

    }

    // TODO compare with System.arrayCopy 
    private static void copyBytes(byte[] source, byte[] dest, int destOffs) {
        for(int i = 0; i != source.length; ++i) {
            dest[destOffs + i] = source[i];
        }        
    }

    // TODO compare with System.arrayCopy 
    private static void copyBytes(byte[] source, int srcOffs, byte[] dest) {
        for(int i = 0; i != dest.length; ++i) {
            dest[i] = source[srcOffs + i];
        }        
    }
    
    private static class ByteArrayComparator implements Comparator<byte[]> {

        public static ByteArrayComparator INSTANCE = new ByteArrayComparator();
        
        @Override
        public int compare(byte[] o1, byte[] o2) {
            for(int i = 0; i != Math.min(o1.length, o2.length); ++i) {
                int cmp = o1[i] - o2[i];
                if (cmp != 0) {
                    return cmp;
                }
            }
            return o1.length - o2.length;
        }        
    }
}
