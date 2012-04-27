package org.gridkit.coherence.misc.bulletproof;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

public interface CacheVerificationStrategy {
	
	public Instance bind(NamedCache cache);
	
	public interface Instance {

		Filter includingCanery(Filter filter);
		
	}

	public interface HardenedRequest<V> {
		
		void perform();
		
		void verify() throws RequestVerificationExcpetion;
		
		void recover();
		
		V getResult();		
	}
}
