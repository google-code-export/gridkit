package org.gridkit.coherence.search.lucene;

import org.apache.lucene.search.Query;
import org.gridkit.coherence.search.SearchFactory;

public class LuceneSearchFactory extends SearchFactory<LuceneInMemoryIndex, LuceneIndexConfig, Query> {

	public LuceneSearchFactory(LuceneDocumentExtractor luceneExtractor) {
		super(new LuceneSearchPlugin(), new DefaultLuceneIndexConfig(), luceneExtractor);
	}

	public void setLuceneIndexConfig(LuceneIndexConfig config) {
		indexConfig = config;
	}
}
