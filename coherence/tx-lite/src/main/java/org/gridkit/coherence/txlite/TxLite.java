package org.gridkit.coherence.txlite;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class TxLite {

	private static ConcurrentMap<String, TxSuperviser> supervizers = new ConcurrentHashMap<String, TxSuperviser>();
	
	public static TxManager getManager() {
		return getManager("tx-lite-system-cache");
	}

	public static TxManager getManager(String systemCache) {
		if (supervizers.get(systemCache) == null) {
			TxSuperviser sv = createSuperviser(systemCache);
			supervizers.putIfAbsent(systemCache, sv);
		}
		TxSuperviser sv = supervizers.get(systemCache);
		return new TxManager(sv);
	}

	private static TxSuperviser createSuperviser(String systemCache) {
		NamedCache cache = CacheFactory.getCache(systemCache);
		TxSuperviser sv = new TxSuperviser(cache);
		// TODO how to manage sweepers;
//		TxSweeper txSweeper = new TxSweeper(sv);
//		txSweeper.start();
		return sv;
	}
}
