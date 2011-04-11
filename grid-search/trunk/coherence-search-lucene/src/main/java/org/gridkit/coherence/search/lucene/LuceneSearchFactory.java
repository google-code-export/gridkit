package org.gridkit.coherence.search.lucene;

import org.apache.lucene.search.Query;
import org.gridkit.coherence.search.SearchFactory;

import com.tangosol.util.Filter;

public class LuceneSearchFactory extends SearchFactory<LuceneInMemoryIndex, LuceneIndexConfig, Query> {

	public LuceneSearchFactory(LuceneDocumentExtractor luceneExtractor) {
		super(new LuceneSearchPlugin(), new DefaultLuceneIndexConfig(), luceneExtractor);
	}

	public void setLuceneIndexConfig(LuceneIndexConfig config) {
		indexConfig = config;
	}

	@Override
	public Filter createFilter(Query query) {
		return new LuceneQueryFilter(createFilterExtractor(), query);
	}
}
