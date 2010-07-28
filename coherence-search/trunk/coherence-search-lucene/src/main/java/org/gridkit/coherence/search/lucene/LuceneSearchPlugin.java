/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.search.lucene;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.gridkit.coherence.search.IndexEngineConfig;
import org.gridkit.coherence.search.IndexInvocationContext;
import org.gridkit.coherence.search.IndexUpdateEvent;
import org.gridkit.coherence.search.PlugableSearchIndex;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LuceneSearchPlugin implements PlugableSearchIndex<LuceneInMemoryIndex, LuceneIndexConfig, Query> {

	private String name = "";
	
	public LuceneSearchPlugin() {
	}
	
	public LuceneSearchPlugin(String name) {
		this.name = name;
	}

	@Override
	public Object createIndexCompatibilityToken(LuceneIndexConfig indexConfig) {
		return new GenericNamedLuceneIndexToken(name);
	}

	@Override
	public void configure(IndexEngineConfig config) {
		config.setOldValueOnUpdateEnabled(false);
	}

	@Override
	public LuceneInMemoryIndex createIndexInstance(LuceneIndexConfig indexConfig) {
		Directory directory = indexConfig.createDirectoryInstance();
		Analyzer analyzer = indexConfig.getAnalyzer(); 
		return new LuceneInMemoryIndex(directory, analyzer);
	}

	@Override
	public void updateIndexEntries(LuceneInMemoryIndex index, Map<Object, IndexUpdateEvent> events, IndexInvocationContext context) {
		index.update(events, context);
	}

	@Override
	public boolean applyIndex(LuceneInMemoryIndex index, Query query, final Set<Object> keySet, IndexInvocationContext context) {
		index.applyIndex(query, keySet, context);
		return false;
	}

	@Override
	public int calculateEffectiveness(LuceneInMemoryIndex index, Query query, Set<Object> keySet, IndexInvocationContext context) {
		// lucene index does not support evaluate method, so index should be used in all cases
		return 1;
	}

	@Override
	public boolean evaluate(Query query, Object document) {
		// TODO we can create new new index put single document and query this index
		return false;
	}
}
