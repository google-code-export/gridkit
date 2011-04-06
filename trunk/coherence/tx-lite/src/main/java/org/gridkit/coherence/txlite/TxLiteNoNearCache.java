package org.gridkit.coherence.txlite;

import java.util.Map;

import com.tangosol.net.NamedCache;

public class TxLiteNoNearCache extends TxCacheWrapper {

//	private NameCache
	
	public TxLiteNoNearCache(Map mapFront, NamedCache mapBack) {
		super(mapBack, new DirtyReadCacheAccessAdapter());
	}	

	public TxLiteNoNearCache(Map mapFront, NamedCache mapBack, int nStrategy) {
		super(mapBack, new DirtyReadCacheAccessAdapter());
	}
}
