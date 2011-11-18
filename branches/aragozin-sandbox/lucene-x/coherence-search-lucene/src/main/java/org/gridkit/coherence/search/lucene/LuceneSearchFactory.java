package org.gridkit.coherence.search.lucene;

import org.apache.lucene.search.Query;
import org.gridkit.coherence.search.SearchFactory;

import com.tangosol.coherence.component.net.extend.remoteService.RemoteCacheService;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.AlwaysFilter;

public class LuceneSearchFactory extends SearchFactory<LuceneInMemoryIndex, LuceneIndexConfig, Query> {

	public LuceneSearchFactory(ValueExtractor luceneExtractor) {
		super(new LuceneSearchPlugin(), new DefaultLuceneIndexConfig(), luceneExtractor);
	}

	public void setLuceneIndexConfig(LuceneIndexConfig config) {
		indexConfig = config;
	}

	@Override
	public Filter createFilter(Query query) {
		return new LuceneQueryFilter(createFilterExtractor(), query);
	}
	
	public <K, V> ScoredEntries<K, V> search(NamedCache cache, Query query, int docLimit) {
		LuceneTopDocSearch processor = new LuceneTopDocSearch(query, docLimit);
		@SuppressWarnings("unchecked")
		ScoredEntries<K,V> entries = broadcast(cache, processor);
		return entries;
	}

	public <S1, S2> S2 broadcast(NamedCache cache, DistributedLuceneIndexProcessor<S1, S2> indexProcessor) {
		LuceneIndexProcessorAgent agent = new LuceneIndexProcessorAgent(indexProcessor, createFilterExtractor());
		if (cache.getCacheService() instanceof DistributedCacheService || cache.getCacheService() instanceof RemoteCacheService) {
			@SuppressWarnings("unchecked")
			S2 result = (S2) cache.aggregate(AlwaysFilter.INSTANCE, agent);
			return result;
		}
		else {
			// TODO support for non distributed caches via filter
			throw new UnsupportedOperationException();
		}
	}
 }
