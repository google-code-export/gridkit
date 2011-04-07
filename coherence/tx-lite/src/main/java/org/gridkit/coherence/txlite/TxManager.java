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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap.EntryProcessor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("deprecation")
public class TxManager {

	private TxSuperviser superviser;
	
	public TxManager(TxSuperviser txSuperviser) {
		this.superviser = txSuperviser;
	}

	public NamedCache toReadCommited(NamedCache cache) {
		if (cache instanceof TxWrappedCache) {
			return toReadCommited(((TxWrappedCache)cache).getVersionedCache());
		}
		return new TxCacheWrapper(cache, new ReadCommitedCacheAccessAdapter(superviser));
	}

	public NamedCache toDirtyRead(NamedCache cache) {
		if (cache instanceof TxWrappedCache) {
			return toDirtyRead(((TxWrappedCache)cache).getVersionedCache());
		}
		return new TxCacheWrapper(cache, new DirtyReadCacheAccessAdapter());
	}
	
	public TxSession openReadOnlySession() {
		return new ReadOnlyTxSession(superviser);
	}
	
	public TxSession openReadWriteSession() {
		return new ReadWriteTxSession(superviser);
	}
	
	private static class ReadOnlyTxSession extends BaseCacheAccessAdapter implements TxSession {
		
		protected TxSuperviser superviser;
		protected int readVersion = Versions.BASELINE_VERSION;
		
		private Map<NamedCache, TxCacheWrapper> wrapperCaches = new HashMap<NamedCache, TxCacheWrapper>();
		
		public ReadOnlyTxSession(TxSuperviser superviser) {
			this.superviser = superviser;
		}

		@Override
		public void close() {
			readVersion = Versions.BASELINE_VERSION;
			wrapperCaches.clear();
		}

		@Override
		public void commit() {
			readVersion = Versions.BASELINE_VERSION;
		}

		@Override
		public void rollback() {
			readVersion = Versions.BASELINE_VERSION;
		}
		
		@Override
		public NamedCache connect(NamedCache cache) {
			if (cache instanceof TxWrappedCache) {
				return connect(((TxWrappedCache)cache).getVersionedCache());
			}
			// TODO check if valid transactional, cache
			TxCacheWrapper wrapper = wrapperCaches.get(cache);
			if (wrapper == null) {
				wrapper = new TxCacheWrapper(cache, this);
				wrapperCaches.put(cache, wrapper);
			}
			return wrapper;
		}

		@Override
		protected int getVersion() {
			return readVersion;
		}

		@Override
		public void beforeOperation(TxCacheWrapper wrapper) {
			if (readVersion == Versions.BASELINE_VERSION) {
				readVersion = superviser.getLatestCommited();
			}
		}

		@Override
		public void afterOperation(TxCacheWrapper wrapper) {
			// TODO can reset version here to simulate statement consitent isolation
		}

		@Override
		public boolean isReadOnly(TxCacheWrapper wrapper) {
			return true;
		}
	}

	private static class ReadWriteTxSession extends ReadOnlyTxSession {
		
		public ReadWriteTxSession(TxSuperviser superviser) {
			super(superviser);
		}
		
		@Override
		public void commit() {
			superviser.commitWriteTx();
			readVersion = Versions.BASELINE_VERSION;
		}
		
		@Override
		public void rollback() {
			superviser.rollbackTxUpdates();
			readVersion = Versions.BASELINE_VERSION;
		}
		
		@Override
		protected int getVersion() {
			return readVersion;
		}
		
		@Override
		public void beforeOperation(TxCacheWrapper wrapper) {
			if (readVersion == Versions.BASELINE_VERSION) {
				readVersion = superviser.openWriteTx();
//				readVersion = superviser.getLatestCommited();
			}
		}
		
		@Override
		public void afterOperation(TxCacheWrapper wrapper) {
			// do nothing
		}
		
		@Override
		public boolean isReadOnly(TxCacheWrapper wrapper) {
			return false;
		}

		@Override
		public void markDirty(TxCacheWrapper txCacheWrapper, Object key) {
			superviser.markKeyForUpdate(txCacheWrapper.getCacheName(), key);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void markDirty(TxCacheWrapper txCacheWrapper, Collection keys) {
			superviser.markKeysForUpdate(txCacheWrapper.getCacheName(), keys);
		}

		@Override
		@SuppressWarnings("unchecked")
		public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Map content) {
			return new VersionedPutProcessor(readVersion, content);
		}

		@Override
		public EntryProcessor newPutProcessor(TxCacheWrapper txCacheWrapper, Object key, Object value) {
			return new VersionedPutProcessor(readVersion, Collections.singletonMap(key, value));
		}

		@Override
		public EntryProcessor transformProcessor(TxCacheWrapper txCacheWrapper,	EntryProcessor agent) {
			return TxUtils.transformMutatorProcessor(agent, readVersion);
		}
	}
}
