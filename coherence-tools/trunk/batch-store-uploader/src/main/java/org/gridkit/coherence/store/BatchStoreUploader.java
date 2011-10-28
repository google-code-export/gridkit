package org.gridkit.coherence.store;

import java.util.HashMap;
import java.util.Map;

import com.tangosol.io.Serializer;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.UID;

public class BatchStoreUploader {
	private int perMemberPutBatchSize;

	private NamedCache cache;
    
    private Map<UID, Map<Object, Object>> nodeBuffer = new HashMap<UID, Map<Object, Object>>();
    
    public BatchStoreUploader(NamedCache cache) {
    	this(cache, 32);
    }

    public BatchStoreUploader(NamedCache cache, int perNodePutBatchSize) {
    	this.cache = cache;
    	this.perMemberPutBatchSize = perNodePutBatchSize;
    }
    
	private void push(Map<Object, Object> batch) {
        Serializer serializer = cache.getCacheService().getSerializer();
        Map<Binary, Binary> binMap = new HashMap<Binary, Binary>(batch.size());
        for(Map.Entry<Object, Object> entry: batch.entrySet()) {
                Binary key = ExternalizableHelper.toBinary(entry.getKey(), serializer);
                Binary value = ExternalizableHelper.toBinary(entry.getValue(), serializer);
                binMap.put(key, value);
        };
        cache.invokeAll(batch.keySet(), new BatchStoreProcessor(binMap));
	}

	public void put(Object key, Object value) {
	        Member member = ((PartitionedService)cache.getCacheService()).getKeyOwner(key);
	        UID id = member.getUid();
	        Map<Object, Object> buf = nodeBuffer.get(id);
	        if (buf == null) {
	                buf = new HashMap<Object, Object>();
	                nodeBuffer.put(id, buf);
	        }
	        buf.put(key, value);
	        if (buf.size() >= perMemberPutBatchSize) {
	        	push(buf);
                buf.clear();
	        }
	}
	
	public <K, V> void putAll(Map<K, V> map) {
	        for(Map.Entry<K, V> entry: map.entrySet()) {
	                put(entry.getKey(), entry.getValue());
	        }
	}
	
	public void flush() {
		for (Map<Object, Object> buf : nodeBuffer.values()) {
	        push(buf);
	        buf.clear();
		}
	}
}
