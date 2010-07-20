package org.gridkit.coherence.search;

import com.tangosol.util.Binary;

/**
 * This a helper interface providing some additional methods in context
 * of index operations invocation.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
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
