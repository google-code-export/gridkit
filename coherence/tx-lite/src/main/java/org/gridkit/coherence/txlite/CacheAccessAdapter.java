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

	@SuppressWarnings("deprecation")
	public Object transformValue(TxCacheWrapper wrapper, ValueContatiner vc);
	
	/**
	 * Marks key as dirty (modified during transaction).
	 */
	public void markDirty(TxCacheWrapper txCacheWrapper, Object key);

	/**
	 * Marks keys as dirty (modified during transaction).
	 */
	@SuppressWarnings("unchecked")
	public void markDirty(TxCacheWrapper txCacheWrapper, Collection keys);

	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Object key, Object value);

	@SuppressWarnings("unchecked")
	public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Map content);
}
