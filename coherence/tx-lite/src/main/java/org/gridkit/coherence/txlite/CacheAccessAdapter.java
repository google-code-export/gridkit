package org.gridkit.coherence.txlite;

import java.util.Collection;
import java.util.Map;

import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.EntryProcessor;

interface CacheAccessAdapter {

	public boolean isReadOnly(TxCacheWrapper wrapper);
	
	public void beforeOperation(TxCacheWrapper wrapper);
	
	public void afterOperation(TxCacheWrapper wrapper);
	
	public ValueExtractor transformIndexExtractor(TxCacheWrapper wrapper, ValueExtractor extractor);

	public Filter transformFilter(TxCacheWrapper wrapper, Filter filter);

	public Filter transformListenerFilter(TxCacheWrapper txCacheWrapper, Filter filter);

	public EntryAggregator transformAggregator(TxCacheWrapper wrapper, EntryAggregator agent);

	public MapListener transformListener(TxCacheWrapper txCacheWrapper, MapListener listener);

	public EntryProcessor transformProcessor(TxCacheWrapper txCacheWrapper,	EntryProcessor agent);

	public Object transformValue(TxCacheWrapper wrapper, ValueContatiner vc);
	
	/**
	 * Marks key as dirty (modified during transaction).
	 */
	public void markDirty(TxCacheWrapper txCacheWrapper, Object key);

	/**
	 * Marks keys as dirty (modified during transaction).
	 */
	public void markDirty(TxCacheWrapper txCacheWrapper, Collection keys);

	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Object key, Object value);

	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Map content);
}
