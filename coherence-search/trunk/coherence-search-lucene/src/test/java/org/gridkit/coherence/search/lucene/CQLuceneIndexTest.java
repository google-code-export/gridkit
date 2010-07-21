package org.gridkit.coherence.search.lucene;



import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.filter.AlwaysFilter;

public class CQLuceneIndexTest extends BaseLuceneIndexTest {

	@Override
	protected NamedCache getCache() {
		return new ContinuousQueryCache(CacheFactory.getCache("local-cache"), new AlwaysFilter());
	}

	
}
