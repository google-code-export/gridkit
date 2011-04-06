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

import com.tangosol.util.Binary;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.gridkit.coherence.search.IndexInvocationContext;
import org.gridkit.coherence.search.IndexUpdateEvent;
import org.gridkit.coherence.search.IndexUpdateEvent.Type;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class LuceneInMemoryIndex {

	public static final String DOCUMENT_KEY = "@doc-key";
	
	private Directory storage;
	private Analyzer analyzer;
	
	private IndexSearcher searcher;
	
	public LuceneInMemoryIndex(Directory directory, Analyzer analyzer) {
		this.storage = directory;
		this.analyzer = analyzer;
	}
	
	public synchronized void update(Map<Object, IndexUpdateEvent> events, IndexInvocationContext ctx) {
		IndexWriter writer = null;

		try {
			if (searcher != null) {
				searcher.close();
				searcher = null;
			}

			writer = new IndexWriter(storage, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
			try {
				for(IndexUpdateEvent event: events.values()) {
					if (event.getType() == Type.NOPE) {
						continue;
					}
					
					String keyHex = toBase64(ctx.ensureBinaryKey(event.getKey()).toByteArray());
					
					switch(event.getType()) {
						case UPDATE:
							writer.deleteDocuments(new Term(DOCUMENT_KEY, keyHex));
						case INSERT:
							Document doc = makeDoc(keyHex, (Field[])event.getValue());
							writer.addDocument(doc);
							break;
						case DELETE:
							writer.deleteDocuments(new Term(DOCUMENT_KEY, keyHex));
					}
				}
			}
			finally {
				writer.optimize();
				writer.close();
				searcher = new IndexSearcher(storage);
			}
			
		} catch (IOException e) {
			// should not happen with RAMDirectory
			throw new RuntimeException(e);
		}
	}

	public synchronized void applyIndex(Query query, final Set<Object> keySet,	final IndexInvocationContext context) {
		if (searcher == null) {
			// index is empty
			keySet.clear();
			return;
		}
		final Set<Object> retained = new HashSet<Object>();		
		try {
			searcher.search(query, new Collector() {
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {
					// ignore
				}
				
				@Override
				public void setNextReader(IndexReader reader, int docBase) throws IOException {
					// ignore
				}
				
				@Override
				@SuppressWarnings("unchecked")
				public void collect(int doc) throws IOException {
					Document document = searcher.doc(doc, new SetBasedFieldSelector(Collections.singleton(LuceneInMemoryIndex.DOCUMENT_KEY), Collections.EMPTY_SET));
					String key64 = document.get(LuceneInMemoryIndex.DOCUMENT_KEY);
					Binary bin = new Binary(fromBase64(key64));
					Object key = context.ensureFilterCompatibleKey(bin);
					if (keySet.contains(key)) {
						retained.add(key);
					}
				}
				
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}
			});
		} catch (IOException e) {
			// should never happen with RAMDirectory
			throw new RuntimeException(e);
		}
		keySet.retainAll(retained);
	}	
	
    private Document makeDoc(String keyHex, Field[] value) {
		Document doc = new Document();
		for(int i = 0; i != value.length; ++i) {
			doc.add(value[i]);
		}
		doc.add(new Field(DOCUMENT_KEY, keyHex, Store.YES, Index.NOT_ANALYZED));
		return doc;
	}

    private String toBase64(byte[] bytes) {
    	return Base64.byteArrayToBase64(bytes);
    }

    private byte[] fromBase64(String text) {
    	return Base64.base64ToByteArray(text);
    }
}
