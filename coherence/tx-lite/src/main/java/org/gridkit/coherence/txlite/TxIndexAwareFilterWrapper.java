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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.tangosol.util.Filter;
import com.tangosol.util.filter.IndexAwareFilter;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class TxIndexAwareFilterWrapper extends TxFilterWrapper implements IndexAwareFilter {

	private static final long serialVersionUID = 20110407L;

	@SuppressWarnings("unchecked")
	private transient Map originalIndexMap = null;
	@SuppressWarnings("unchecked")
	private transient Map convertedIndexMap = null;
	
	public TxIndexAwareFilterWrapper(IndexAwareFilter nested, int readVesrion) {
		super(nested, readVesrion);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized Filter applyIndex(Map indexMap, Set keys) {
		convertIndexMap(indexMap);
		((IndexAwareFilter)nested).applyIndex(convertedIndexMap, keys);
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized int calculateEffectiveness(Map indexMap, Set keys) {
		convertIndexMap(indexMap);
		return ((IndexAwareFilter)nested).calculateEffectiveness(convertedIndexMap, keys);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean evaluateEntry(Entry entry) {
		return evaluate(entry.getValue());
	}

	@SuppressWarnings("unchecked")
	private void convertIndexMap(Map indexMap) {
		if (indexMap == originalIndexMap) {
			return;
		}
		else {
			Map<?, ?> map = new HashMap(indexMap);
			for (Map.Entry entry : map.entrySet()) {
				TxIndexExtractorWrapper extr = (TxIndexExtractorWrapper) entry.getValue();
				entry.setValue(extr.getValueExtractor());
			}
		}
	}
}
