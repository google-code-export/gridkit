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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.gridkit.coherence.search.IndexEngineConfig;
import org.gridkit.coherence.search.IndexInvocationContext;
import org.gridkit.coherence.search.IndexUpdateEvent;
import org.gridkit.coherence.search.PlugableSearchIndex;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class LuceneSearchPlugin implements PlugableSearchIndex<LuceneInMemoryIndex, LuceneIndexConfig, Query>, Serializable, PortableObject {

	private static final long serialVersionUID = 20100813L;
	
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
		return 1;
	}

	@Override
	public boolean evaluate(Query query, Object document) {
		Field[] fields = (Field[]) document;
		MemoryIndex memIndex = new MemoryIndex();
		for(Field field: fields) {
			memIndex.addField(field.name(), field.tokenStreamValue(), field.getBoost());
		}
		return memIndex.search(query) > 0.0f;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		name = in.readString(1);
		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(1, name);		
	}
}
