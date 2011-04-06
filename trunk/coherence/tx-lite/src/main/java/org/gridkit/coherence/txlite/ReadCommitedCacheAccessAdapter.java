package org.gridkit.coherence.txlite;

import com.tangosol.net.NamedCache;

public class ReadCommitedCacheAccessAdapter extends BaseCacheAccessAdapter {

	private TxSuperviser superviser;
	
	public ReadCommitedCacheAccessAdapter(TxSuperviser superviser) {
		this.superviser = superviser;
	}
	
	@Override
	protected int getVersion() {
		return superviser.getLatestCommited();
	}

	@Override
	public void afterOperation(TxCacheWrapper wrapper) {
		// do nothing
	}

	@Override
	public void beforeOperation(TxCacheWrapper wrapper) {
		// do nothing
	}
}
