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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.gridkit.coherence.search.IndexInvocationContext;
import org.gridkit.coherence.search.IndexUpdateEvent;
import org.gridkit.coherence.search.IndexUpdateEvent.Type;

import com.tangosol.util.Binary;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class LuceneInMemoryIndex {

	public static final String DOCUMENT_KEY = "@doc-key";
	public static final FieldSelector DOCUMENT_KEY_SELECTOR = new DocKeyFieldSelector();
	
	private Directory storage;
	private Analyzer analyzer;
	
	private IndexWriter writer;
	private IndexSearcher searcher;
	
	public LuceneInMemoryIndex(Directory directory, Analyzer analyzer) {
		this.storage = directory;
		this.analyzer = analyzer;
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, analyzer);
		try {
			writer = new IndexWriter(storage, iwc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void update(Map<Object, IndexUpdateEvent> events, IndexInvocationContext ctx) {
		try {
			if (searcher != null) {
				searcher.close();
				searcher = null;
			}

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
//				writer.optimize();
//				writer.close();
				writer.commit();
				searcher = new IndexSearcher(IndexReader.open(writer, true));
			}
			
		} catch (IOException e) {
			// should not happen with RAMDirectory
			throw new RuntimeException(e);
		}
	}

	public synchronized void applyIndex(Query query, final Set<Object> keySet, final IndexInvocationContext context) {
		if (searcher == null) {
			// index is empty
			keySet.clear();
			return;
		}
		final Set<Object> retained = new HashSet<Object>();		
		try {
			searcher.search(query, new Collector() {
				
				IndexReader reader;
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {
					// ignore
				}
				
				@Override
				public void setNextReader(IndexReader reader, int docBase) throws IOException {
					this.reader = reader; 
				}
				
				@Override
				public void collect(int doc) throws IOException {
					Document document = reader.document(doc, DOCUMENT_KEY_SELECTOR);
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
	
    public synchronized IndexSearcher getSearcher() {
		try {
			IndexSearcher searcher = new IndexSearcher(storage);
			return searcher;
		} catch (CorruptIndexException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
	private Document makeDoc(String keyHex, Field[] value) {
		Document doc = new Document();
		for(int i = 0; i != value.length; ++i) {
			doc.add(value[i]);
		}
		doc.add(new Field(DOCUMENT_KEY, keyHex, Store.YES, Index.NOT_ANALYZED_NO_NORMS));
		return doc;
	}

    private String toBase64(byte[] bytes) {
    	return Base64.byteArrayToBase64(bytes);
    }

    private byte[] fromBase64(String text) {
    	return Base64.base64ToByteArray(text);
    }    
    
    public static class DocKeyFieldSelector implements FieldSelector {

    	private static final long serialVersionUID = 20110823L;

		@Override
		public FieldSelectorResult accept(String fieldName) {
			if (DOCUMENT_KEY.equals(fieldName)) {
				return FieldSelectorResult.LOAD_AND_BREAK;
			}
			else {
				return FieldSelectorResult.NO_LOAD;
			}
		}
    }
}
