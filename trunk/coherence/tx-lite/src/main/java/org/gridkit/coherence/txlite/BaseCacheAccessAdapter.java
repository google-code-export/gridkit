package org.gridkit.coherence.txlite;

import java.util.Collection;
import java.util.Map;

import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.EntryProcessor;

abstract class BaseCacheAccessAdapter implements CacheAccessAdapter {
	
	protected abstract int getVersion();

	@Override
	public EntryAggregator transformAggregator(TxCacheWrapper wrapper, EntryAggregator agent) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Filter transformFilter(TxCacheWrapper wrapper, Filter filter) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public ValueExtractor transformIndexExtractor(TxCacheWrapper wrapper, ValueExtractor extractor) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Object transformValue(TxCacheWrapper wrapper, ValueContatiner vc) {
		return vc.getVersionAt(getVersion());
	}

	@Override
	public boolean isReadOnly(TxCacheWrapper wrapper) {
		return true;
	}

	@Override
	public void markDirty(TxCacheWrapper txCacheWrapper, Collection keys) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public void markDirty(TxCacheWrapper txCacheWrapper, Object key) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Map content) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Object key, Object value) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public MapListener transformListener(TxCacheWrapper txCacheWrapper,	MapListener listener) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Filter transformListenerFilter(TxCacheWrapper txCacheWrapper, Filter filter) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public EntryProcessor transformProcessor(TxCacheWrapper txCacheWrapper,	EntryProcessor agent) {
		// TODO
		throw new UnsupportedOperationException();
	}
}
