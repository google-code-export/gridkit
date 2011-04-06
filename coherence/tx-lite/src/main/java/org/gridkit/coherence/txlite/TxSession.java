package org.gridkit.coherence.txlite;

import com.tangosol.net.NamedCache;

// TODO hide internal API
public interface TxSession {

	public NamedCache connect(NamedCache cache); 
	
	public void commit();
	
	public void rollback();
	
	public void close();
	
}
