/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.offheap.storage;

import java.util.Iterator;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface ObjectStore {
    
    public Iterator<Object> keys();
    public Object load(Object key);
    public void store(Object key, Object value);
    public void erase(Object key);
    public void eraseAll();
    public int size();
}
