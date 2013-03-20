package org.gridkit.coherence.events;

import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.MapListener;

public class ProcessingCacheMap extends LocalCache {

	private MapListener listener;
	
	private ProcessingCacheMap() {
		super();
	}
	
	@Override
	public synchronized void addMapListener(MapListener listener) {
		super.addMapListener(listener);
		System.out.println("Listener added: " + listener);
		this.listener = listener;
	}
	
	@Override
	public Object put(Object oKey, Object oValue) {
		System.out.println("PUT at " + oKey + ", listener: " + listener);
		return super.put(oKey, oValue);
	}
}
