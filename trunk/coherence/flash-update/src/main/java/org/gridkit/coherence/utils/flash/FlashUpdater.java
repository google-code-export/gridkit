package org.gridkit.coherence.utils.flash;

import java.util.HashMap;
import java.util.Map;

import com.tangosol.io.Serializer;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.UID;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * A helper class for flash-update implementation.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class FlashUpdater {
	
	private int decorationNo = ExternalizableHelper.DECO_CUSTOM;
	private int putBatchSize = 32;
	
	private NamedCache cache;
	
	private Map<UID, Map<Object, Object>> nodeBuffer = new HashMap<UID, Map<Object, Object>>();
	
	private boolean dirty = false;
	
	public FlashUpdater(NamedCache cache) {
		this.cache = cache;
	}

	/**
	 * Size limit for bulk updates.
	 * @param size
	 */
	public void setBatchSize(int size) {
		this.putBatchSize = size;
	}

	/**
	 * Sets decoration ID used to temporary store new value.
	 * @param decorationNo
	 */
	public void setDecorationNo(int decorationNo) {
		if (dirty) {
			throw new IllegalStateException("Operation in progress");
		}
		this.decorationNo = decorationNo;
	}
	
	/**
	 * Replace all old values by new ones, via singe operation.
	 */
	public void commit() {
		for(Map.Entry<UID, Map<Object, Object>> entry : nodeBuffer.entrySet()) {
			push(entry.getValue());
		}
		nodeBuffer.clear();
		cache.invokeAll(AlwaysFilter.INSTANCE, new CommitEntryProcessor(decorationNo));
		dirty = false;
	}
	
	private void push(Map<Object, Object> batch) {
		Serializer serializer = cache.getCacheService().getSerializer();
		Map<Binary, Binary> binMap = new HashMap<Binary, Binary>(batch.size());
		for(Map.Entry<Object, Object> entry: batch.entrySet()) {
			Binary key = ExternalizableHelper.toBinary(entry.getKey(), serializer);
			Binary value = ExternalizableHelper.toBinary(entry.getValue(), serializer);
			binMap.put(key, value);
		};
		this.cache.invokeAll(batch.keySet(), new PushEntryProcessor(binMap, decorationNo));
	}

	/**
	 * Put new value to cache. To remove existing key, you should put <code>null</code> as new value.
	 * None of changes will be visible until you call {@link #commit()}.
	 */
	public void put(Object key, Object value) {
		dirty = true;
		Member member = ((PartitionedService)cache.getCacheService()).getKeyOwner(key);
		UID id = member.getUid();
		Map<Object, Object> buf = nodeBuffer.get(id);
		if (buf == null) {
			buf = new HashMap<Object, Object>();
			nodeBuffer.put(id, buf);
		}
		buf.put(key, value);
		if (buf.size() >= putBatchSize) {
			push(buf);
			buf.clear();
		}
	}
	
	/**
	 * Put new values to cache. To remove existing key, you should put <code>null</code> as new value.
	 * None of changes will be visible until you call {@link #commit()}.
	 */
	public <K, V> void putAll(Map<K, V> map) {
		for(Map.Entry<K, V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Remove uncommitted values from cache. Useful if there is a chance what previous flash-update wasn't finished.   
	 */
	public void cleanup() {
		cache.invokeAll(AlwaysFilter.INSTANCE, new CleanupEntryProcessor(decorationNo));
		dirty = false;
	}
	
	public static class PushEntryProcessor extends AbstractProcessor {
		
		private static final long serialVersionUID = 20101026L;
		
		private int decorationNo;
		private Map<Binary, Binary> values;
		
		public PushEntryProcessor(Map<Binary, Binary> values, int decorationNo) {
			this.values = values;
			this.decorationNo = decorationNo;
		}

		@Override
		public Object process(Entry e) {
			BinaryEntry be = (BinaryEntry)e;
			Binary binaryKey = be.getBinaryKey();
			Binary newValue = values.get(binaryKey);
			if (newValue != null) {
				Binary value = be.getBinaryValue();
				value = ExternalizableHelper.decorate(value, decorationNo, newValue);
				be.updateBinaryValue(value);
			}
			return null;
		}
	}

	public static class CommitEntryProcessor extends AbstractProcessor {

		private static final long serialVersionUID = 20101026L;
		
		private int decorationNo;
		
		public CommitEntryProcessor(int decorationNo) {
			this.decorationNo = decorationNo;
		}
		
		@Override
		public Object process(Entry e) {
			BinaryEntry be = (BinaryEntry)e;
			Binary oldValue = be.getBinaryValue();
			Binary newValue = ExternalizableHelper.getDecoration(oldValue, decorationNo);
			if (newValue != null) {
				if (newValue.length() == 0) {
					be.remove(false);
				}
				else {
					be.updateBinaryValue(newValue);
				}
			}
			return null;
		}
	}
	
	public static class CleanupEntryProcessor extends AbstractProcessor {

		private static final long serialVersionUID = 20101026L;
		
		private int decorationNo;
		
		public CleanupEntryProcessor(int decorationNo) {
			this.decorationNo = decorationNo;
		}
		
		@Override
		public Object process(Entry e) {
			BinaryEntry be = (BinaryEntry)e;
			Binary oldValue = be.getBinaryValue();
			Binary newValue = ExternalizableHelper.getDecoration(oldValue, decorationNo);
			if (newValue != null) {
				Binary nv = ExternalizableHelper.undecorate(oldValue, decorationNo);
				be.updateBinaryValue(nv);
			}
			return null;
		}
	}	
}
