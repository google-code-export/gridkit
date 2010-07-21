package org.gridkit.coherence.search.lucene;



import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class LocalLuceneIndexTest extends BaseLuceneIndexTest {

	@Override
	protected NamedCache getCache() {
		return CacheFactory.getCache("local-cache");
	}

	
}
