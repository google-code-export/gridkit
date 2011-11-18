package org.gridkit.coherence.search.lucene;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

public interface DistributedLuceneIndexProcessor<S1, S2> {

	public static final String DOCUMENT_KEY = LuceneInMemoryIndex.DOCUMENT_KEY;
	
	public S2 executeOnResults(Collection<S1> nodeResults);
	
	public S1 executeOnIndex(IndexAggregationContext context) throws IOException;
	
	public static interface IndexAggregationContext {

		public Object docToKey(Document doc);

		public Object docIdToKey(int docId);

		public IndexSearcher getIndexSearcher();
		
	}
}
