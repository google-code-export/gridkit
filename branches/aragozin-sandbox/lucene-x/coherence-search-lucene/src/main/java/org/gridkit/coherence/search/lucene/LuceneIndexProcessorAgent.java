package org.gridkit.coherence.search.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.gridkit.coherence.search.SearchFactory;
import org.gridkit.coherence.search.SearchFactory.SearchIndexCallable;
import org.gridkit.coherence.search.SearchFactory.SearchIndexWrapper;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.EntryAggregator;
import com.tangosol.util.InvocableMap.ParallelAwareAggregator;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;

public class LuceneIndexProcessorAgent implements ParallelAwareAggregator, PortableObject {

	private static final long serialVersionUID = 20110823L;
	
	private DistributedLuceneIndexProcessor<?, ?> indexProcessor;
	private ValueExtractor indexExtractor;
	
	public LuceneIndexProcessorAgent(DistributedLuceneIndexProcessor<?, ?> indexProcessor, ValueExtractor indexExtractor) {
		this.indexProcessor = indexProcessor;
		this.indexExtractor = indexExtractor;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object aggregate(Set entries) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntryAggregator getParallelAggregator() {
		return new NodeAggregatorAgent(indexProcessor, indexExtractor);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object aggregateResults(Collection results) {
		Set pr = new HashSet(results);
		pr.remove(null);
		return indexProcessor.executeOnResults(results);
	}
	
	@Override
	public void readExternal(PofReader reader) throws IOException {
		this.indexProcessor = (DistributedLuceneIndexProcessor<?, ?>) reader.readObject(1);
		this.indexExtractor = (ValueExtractor) reader.readObject(2);			
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(1, indexProcessor);
		writer.writeObject(2, indexExtractor);			
	}	

	public static class NodeAggregatorAgent implements EntryAggregator, PortableObject {
		
		private static final long serialVersionUID = 20110823L;
		
		private DistributedLuceneIndexProcessor<?, ?> indexProcessor;		
		private ValueExtractor indexExtractor;
		private transient boolean completed = false;

		// PortableObject no-arg constructor
		public NodeAggregatorAgent() {};
		
		public NodeAggregatorAgent(DistributedLuceneIndexProcessor<?, ?> indexProcessor, ValueExtractor indexExtractor) {
			this.indexProcessor = indexProcessor;
			this.indexExtractor = indexExtractor;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public synchronized Object aggregate(Set entries) {
			if (entries.isEmpty() || completed) {
				return null;
			}
			else {
				Map.Entry<?, ?> e = (Entry<?, ?>) entries.iterator().next();
				if (e instanceof BinaryEntry) {
					BinaryEntry be = (BinaryEntry)e;
					Map<ValueExtractor,MapIndex> indexMap = be.getBackingMapContext().getIndexMap();
					MapIndex mi =indexMap.get(indexExtractor);
					if (mi != null) {
						@SuppressWarnings("unchecked")
						IndexContext ctx = new IndexContext((SearchIndexWrapper<LuceneInMemoryIndex>)mi);
						try {							
							Object pr = indexProcessor.executeOnIndex(ctx);
							return pr;
						}
						catch(IOException ex) {
							throw new RuntimeException(ex);
						}
						finally {
							ctx.close();
							completed = true;
						}
					}
					else {
						throw new UnsupportedOperationException("No Lucene index found");
					}
				}
				else {
					throw new UnsupportedOperationException("Can be invoked only on partitioned cache");
				}
			}
		}

		@Override
		public void readExternal(PofReader reader) throws IOException {
			this.indexProcessor = (DistributedLuceneIndexProcessor<?, ?>) reader.readObject(1);
			this.indexExtractor = (ValueExtractor) reader.readObject(2);			
		}

		@Override
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeObject(1, indexProcessor);
			writer.writeObject(2, indexExtractor);			
		}
	}
	
	private static class IndexContext implements DistributedLuceneIndexProcessor.IndexAggregationContext {
		
		private SearchFactory.SearchIndexWrapper<LuceneInMemoryIndex> index;
		private IndexSearcher searcher;
		
		public IndexContext(SearchIndexWrapper<LuceneInMemoryIndex> index) {
			this.index = index;
			SearchIndexCallable<LuceneInMemoryIndex, IndexSearcher> callable = new SearchIndexCallable<LuceneInMemoryIndex, IndexSearcher>() {				
				@Override
				public IndexSearcher execute(LuceneInMemoryIndex li) {
					return li.getSearcher();
				}
			};			
			this.searcher = index.callCoreIndex(callable);			
		}

		@Override
		public Object docIdToKey(int docId) {
			try {
				Document doc = searcher.doc(docId, LuceneInMemoryIndex.DOCUMENT_KEY_SELECTOR);
				String key64 = doc.get(LuceneInMemoryIndex.DOCUMENT_KEY);
				byte[] bin = Base64.base64ToByteArray(key64);
				Object key = index.ensureObjectKey(new Binary(bin));
				return key;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Object docToKey(Document doc) {
			String key64 = doc.get(LuceneInMemoryIndex.DOCUMENT_KEY);
			byte[] bin = Base64.base64ToByteArray(key64);
			Object key = index.ensureObjectKey(new Binary(bin));
			return key;
		}

		@Override
		public IndexSearcher getIndexSearcher() {
			return searcher;
		}
		
		public void close() {
			try {
				searcher.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
