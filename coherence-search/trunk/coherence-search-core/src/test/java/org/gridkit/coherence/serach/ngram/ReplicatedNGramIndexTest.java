package org.gridkit.coherence.serach.ngram;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class ReplicatedNGramIndexTest extends BaseNGramIndexTest {

	@Override
	protected NamedCache getCache() {
		return CacheFactory.getCache("replicated-cache");
	}

	
}
