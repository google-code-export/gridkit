package org.gridkit.coherence.misc.bulletproof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.GuardSupport;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.cache.ReadWriteBackingMap;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.Converter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * {@link EntryProcessor} using {@link PartitionLoader} to recover partition content.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PartitionRecoveryProcessor extends AbstractProcessor implements PortableObject {
    
	private static final long serialVersionUID = 20120411L;

    private static final int READ_THROUGH_CHUNK_LIMIT = Integer.getInteger("gridkit.coherence.recovery-read-chunk-size", 100);
    
    public PartitionRecoveryProcessor() {
    	// Used by POF serializer
    } 

    @Override
	public Object process(InvocableMap.Entry entry) {
    	if (!(entry.getKey() instanceof CanaryKey)) {
    		throw new IllegalArgumentException("Recovery processor can work only with canary key");
    	}
        BinaryEntry bEntry = (BinaryEntry)entry;
        
		if (entry.isPresent() && (Boolean)entry.getValue()) {
			// no recovery needed
			return null;
		}
		
		int partitionId = ((CanaryKey)entry.getKey()).getPartitionId();
	
//		log.info("Recovery started. View name: " + viewName + ", partitionID: " + partitionId);
			
		BackingMapContext bcx = bEntry.getBackingMapContext();
		String cacheName = bcx.getCacheName();						
		try {
		    PartitionLoader loader = getPartitionLoader(bEntry);

		    if (loader != null) {
		        int partitionCount = getPartitionCount(bEntry);
		        
		        // this request may take long, especially in test environment
		        GuardSupport.heartbeat(TimeUnit.MINUTES.toMillis(10));
//			        log.debug("Recovery [View name: " + viewName + ", partitionID: " + partitionId + "] Query DB for partition keys ...");
		        Collection<Object> keys = loader.getKeysForPartition(partitionId, partitionCount);

		        Converter keyConv = bEntry.getContext().getKeyToInternalConverter();
		        Converter valConv = bEntry.getContext().getValueToInternalConverter();
		        
		        int missing = 0;
		        List<Binary> binaryKeys = new ArrayList<Binary>();
		        for (Object key : keys) {
		        	BinaryEntry me = (BinaryEntry) bcx.getBackingMapEntry(keyConv.convert(key));
		        	if (!me.isPresent()) {
		        		binaryKeys.add(me.getBinaryKey());
		        		++missing;
		        	}
		        }			        
		        GuardSupport.heartbeat();
		        
//			        log.debug("Recovery [View name: " + viewName + ", partitionID: " + partitionId + "] Data base has " + dataKeys.size() + " enties, missing in cache " + missing);
		        ReadWriteBackingMap bMap = ((ReadWriteBackingMap)bEntry.getBackingMap());			        
		        int loaded = readThrough(cacheName, bMap, partitionId, binaryKeys);	                
//	                log.debug("Recovery [View name: " + viewName + ", partitionID: " + partitionId + "] Read-through has loaded " + loaded + " entries");
		        
		        if (loaded != binaryKeys.size()) {
		            throw new RuntimeException("Not all keys were loaded during recovery " +
		                                       "of partition " + partitionId + " for cache " + cacheName);
		        }
		        
                // adding canary key after and using backing map to comply cache consistency
                bMap.put(keyConv.convert(entry.getKey()), valConv.convert(true));
		    } else {
		    	// recovery finished, marking partition alive
//			    	log.error("No CacheStore is configured. Assuming test configuration " + partitionId + " for view " + viewName);
		    	entry.setValue(true);
		    }
		} catch (RuntimeException e) {
//			    log.error("Failed to recover partition " + partitionId + " for cache " + cacheName, e);
		    throw e;
		}
//			log.info("Recovery finished. View name: " + cacheName + ", partitionID: " + partitionId);
		return null;
	}
    
    private static int getPartitionCount(BinaryEntry bEntry) {
        return ((PartitionedService)bEntry.getContext().getCacheService()).getPartitionCount();
    }
    
    private int readThrough(String cacheName, ReadWriteBackingMap bMap, int partitionId, List<Binary> keys) {
    	int n = 0;
    	int loaded = 0;
    	GuardSupport.heartbeat();
    	while(n < keys.size()) {
    		int m = Math.min(n + READ_THROUGH_CHUNK_LIMIT, keys.size());
    		List<Binary> chunk = keys.subList(n, m);
    		// TODO debug("Recovery [View name: " + viewName + ", partitionID: " + partitionId + "] Read-through key range, positions [" + n + ", " + m + ")");
    		loaded += bMap.getAll(chunk).size();
    		n = m;
    		GuardSupport.heartbeat();
    	}
    	return loaded;
    }
    
    private PartitionLoader getPartitionLoader(InvocableMap.Entry entry) {
        ObservableMap backingMap = ((BinaryEntry)entry).getBackingMap();
        
        if (backingMap instanceof ReadWriteBackingMap) {
            ReadWriteBackingMap rwBackingMap = (ReadWriteBackingMap)backingMap;
            Object cacheStore = rwBackingMap.getCacheStore().getStore();
            if (cacheStore instanceof PartitionLoader)
                return ((PartitionLoader)cacheStore);
        }
        
        return null;
    }
    
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
    public void readExternal(PofReader reader) throws IOException {
        // no state
    }

    @Override
    public void writeExternal(PofWriter writer) throws IOException {
        // no state
    }
}
