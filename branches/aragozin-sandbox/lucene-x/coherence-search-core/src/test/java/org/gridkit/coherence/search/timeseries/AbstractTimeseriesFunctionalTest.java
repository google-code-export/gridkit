package org.gridkit.coherence.search.timeseries;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public abstract class AbstractTimeseriesFunctionalTest {

	public static NamedCache testCache = CacheFactory.getCache("local-cache");
	
	
	public static SampleKey key(Object key, int ord) {
		return new SampleKey(key, ord);
	}

	public static SampleValue version(Object value, long timestamp) {
		return new SampleValue(timestamp, value);
	}
}
