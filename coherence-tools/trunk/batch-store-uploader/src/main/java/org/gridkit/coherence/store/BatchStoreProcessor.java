package org.gridkit.coherence.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.net.cache.ReadWriteBackingMap.CacheStoreWrapper;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class BatchStoreProcessor extends AbstractProcessor {
	private static final long serialVersionUID = 1L;
	
	private Map<Binary, Binary> payload;
	
	public BatchStoreProcessor(Map<Binary, Binary> payload) {
		this.payload = payload;
	}

	@Override
	public Object process(Entry entry) {
		processAll(Collections.singleton(entry));
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map processAll(Set entries) {
		Map backingMap = null;
		Map<Binary, Binary> candidates = new HashMap<Binary, Binary>(entries.size());
        for(Object e: entries) {
            BinaryEntry entry = (BinaryEntry) e;
            
            if (backingMap == null) {
                backingMap = entry.getBackingMap();
            }
            
            Binary candidateValue = payload.get(entry.getBinaryKey());
            if (candidateValue != null) {
            	candidates.put(entry.getBinaryKey(), candidateValue);
            }
        }

        if (!candidates.isEmpty()) {
	        ReadWriteBackingMap rwmap = (ReadWriteBackingMap) backingMap;
	        ((CacheStoreWrapper)rwmap.getCacheStore()).getCacheStore().storeAll(candidates);
	        Map missesCache = rwmap.getMissesCache();
	        if (missesCache != null) {
		        for (Binary key : candidates.keySet()) {
		        	missesCache.remove(key);
		        }
	        }
	        rwmap.getInternalCache().putAll(candidates);
        }
        
        return Collections.emptyMap();
	}
}
