package org.gridkit.coherence.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class BatchStoreProcessor extends AbstractProcessor {
	private static final long serialVersionUID = 1L;
	
	private Map<Object, Object> payload;
	
	public BatchStoreProcessor(Map<Object, Object> payload) {
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
		Map<Object, Object> candidates = new HashMap<Object, Object>(entries.size());
        for(Object e: entries) {
            BinaryEntry entry = (BinaryEntry) e;
            if (backingMap == null) {
                backingMap = entry.getBackingMap();
            }
            
            Object candidateValue = payload.get(entry.getKey());
            if (candidateValue != null) {
            	candidates.put(entry.getKey(), candidateValue);
            }
        }
        
        ReadWriteBackingMap rwmap = (ReadWriteBackingMap) backingMap;
        rwmap.putAll(candidates);
        
        return Collections.emptyMap();
	}
}
