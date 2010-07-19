package org.gridkit.coherence.search;

import com.tangosol.util.Binary;

public interface IndexInvocationContext {

    public Binary ensureBinaryKey(Object key);    
    public Object ensureObjectKey(Object key);
    
    /**
     * In some cases you may want to keep simple attribute index along woth specialized.
     * This method 
     * @param key
     * @return
     */
    public Object getRawAttribute(Object key);
    
}
