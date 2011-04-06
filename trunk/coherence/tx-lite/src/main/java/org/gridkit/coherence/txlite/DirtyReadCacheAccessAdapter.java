package org.gridkit.coherence.txlite;

public class DirtyReadCacheAccessAdapter extends BaseCacheAccessAdapter {

	@Override
	protected int getVersion() {
		return Versions.LATEST_VERSION;
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
