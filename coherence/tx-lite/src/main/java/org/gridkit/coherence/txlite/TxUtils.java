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

import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.InvocableMap.ParallelAwareAggregator;
import com.tangosol.util.filter.IndexAwareFilter;
import com.tangosol.util.filter.KeyAssociatedFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("deprecation")
class TxUtils {

	public static final Filter transformFilter(Filter f, int readVersion) {
		if (f instanceof KeyAssociatedFilter) {
			KeyAssociatedFilter kaf = (KeyAssociatedFilter)f;
			Filter nested = kaf.getFilter();
			Object hostKey = kaf.getHostKey();
			return new KeyAssociatedFilter(transformFilter(nested, readVersion), hostKey);
		}
		else if (f instanceof IndexAwareFilter){
			return new TxIndexAwareFilterWrapper((IndexAwareFilter) f, readVersion);
		}
		else {
			return new TxFilterWrapper(f, readVersion);
		}
	}
	
	public static final ValueExtractor transformIndexExtractor(ValueExtractor extractor) {
		return new TxIndexExtractorWrapper(extractor); 
	}
	
	public static final EntryAggregator transformAggregator(EntryAggregator agent, int version) {
		if (agent instanceof ParallelAwareAggregator) {
			return new ParallelEntryAggregatorAdapter((ParallelAwareAggregator) agent, version);
		}
		else {
			return new EntryAggregatorAdapter(agent, version);
		}
	}
	
	public static final EntryProcessor transformReadOnlyProcessor(EntryProcessor processor, int version) {
		return new EntryProcessorAdapter(processor, version, true);
	}

	public static final EntryProcessor transformMutatorProcessor(EntryProcessor processor, int version) {
		return new EntryProcessorAdapter(processor, version, false);
	}
}
