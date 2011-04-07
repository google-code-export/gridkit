/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.txlite;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
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
