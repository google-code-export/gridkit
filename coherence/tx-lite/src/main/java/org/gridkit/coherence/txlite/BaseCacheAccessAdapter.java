/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.txlite;

import java.util.Collection;
import java.util.Map;

import com.tangosol.util.Filter;
import com.tangosol.util.MapListener;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.EntryProcessor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("deprecation")
abstract class BaseCacheAccessAdapter implements CacheAccessAdapter {
	
	protected abstract int getVersion();

	@Override
	public EntryAggregator transformAggregator(TxCacheWrapper wrapper, EntryAggregator agent) {
		return TxUtils.transformAggregator(agent, getVersion());
	}

	@Override
	public EntryProcessor transformProcessor(TxCacheWrapper txCacheWrapper,	EntryProcessor agent) {
		return TxUtils.transformReadOnlyProcessor(agent, getVersion());
	}

	@Override
	public Filter transformFilter(TxCacheWrapper wrapper, Filter filter) {
		return TxUtils.transformFilter(filter, getVersion());
	}

	@Override
	public ValueExtractor transformIndexExtractor(TxCacheWrapper wrapper, ValueExtractor extractor) {
		return TxUtils.transformIndexExtractor(extractor);
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
	@SuppressWarnings("unchecked")
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
	@SuppressWarnings("unchecked")
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
}
