package org.gridkit.coherence.txlite;

import com.tangosol.net.NamedCache;

interface TxWrappedCache {

	public NamedCache getVersionedCache();
	
}
