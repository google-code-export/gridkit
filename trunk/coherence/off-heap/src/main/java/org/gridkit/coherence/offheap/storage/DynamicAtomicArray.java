/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface DynamicAtomicArray<E> extends AtomicArray<E> {
    
    /**
     * Sets the length of array.
     */
    public void setLength(int length);
}
