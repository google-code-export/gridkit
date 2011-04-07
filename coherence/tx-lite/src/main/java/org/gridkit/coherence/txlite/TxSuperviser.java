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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.ServiceInfo;
import com.tangosol.util.UID;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("deprecation")
class TxSuperviser {
	
	private static final String TX_COUNTER = "TX_COUNTER";
	private static final String TX_COMMITED = "TX_COMMITED";
	private static final String TX_LOCK = TX_COUNTER;
	private static final String TX_GC_LOCK = "TX_GC_LOCK";
	
	private NamedCache txControl;	
	private Map<Integer, Integer> versionLocks = new HashMap<Integer, Integer>();
	
	private Integer activeTx = null;

	// TODO in JVM multiple write session support
	public TxSuperviser(NamedCache cache) {
		this.txControl = cache;
	}
	
	public int getLatestCommited() {
		Integer val = (Integer) txControl.get(TX_COMMITED);
		return val == null ? 1 : val.intValue();
	}
	
	public synchronized void addReadLock(int version) {
		Integer lockCount = versionLocks.get(version);
		if (lockCount == null) {
			UID uid = txControl.getCacheService().getCluster().getLocalMember().getUid();
			txControl.put(new ReaderLock(uid, version), null);
			versionLocks.put(version, 1);
		}
		else {
			versionLocks.put(version, lockCount + 1);
		}
	}
	
	public synchronized void removeReadLock(int version) {
		Integer lockCount = versionLocks.get(version);
		if (lockCount == null) {
			throw new IllegalStateException("Try to release lock, but lock lock for version " + version + " is established");
		}
		lockCount = lockCount - 1;
		if (lockCount == 0) {
			UID uid = txControl.getCacheService().getCluster().getLocalMember().getUid();
			txControl.remove(new ReaderLock(uid, version));
			versionLocks.remove(version);
		}
	}
	
	boolean accureMaintenanceLock(int timeout) {
		return txControl.lock(TX_GC_LOCK, timeout);
	}

	void releaseMaintenanceLock() {
		txControl.unlock(TX_GC_LOCK);		
	}
	
	int getTxLogSize() {
		// TODO exclude read locks from size
		return txControl.size();
	}

	/**
	 * Accrues write right for current member and returns version
	 * number to be used for update.
	 */
	public synchronized int openWriteTx() {
		return openWriteTx(Long.MAX_VALUE);
	}

	/**
	 * Accrues write right for current member and returns version
	 * number to be used for update.
	 * @param timeout time to wait for write lock
	 * @return write version or -1 in case of time out.
	 */
	public synchronized int openWriteTx(long timeout) {
		if (activeTx != null) {
			throw new IllegalStateException("Transaction already open");
		}
		if (txControl.lock(TX_LOCK, timeout)) {
			Integer txs = (Integer) txControl.get(TX_COUNTER);
			Integer txc = (Integer) txControl.get(TX_COMMITED);
			txs = txs == null ? 1 : txs;
			txc = txc == null ? 1 : txc;
			
			if (txs > txc) {
				activeTx = txs;
				rollbackTxUpdates();
			}
			
			txs = Versions.inc(txs);
			activeTx = txs;
			txControl.put(TX_COUNTER, txs);
			return txs;
		}
		else {
			return -1;
		}
	}

	public void markKeyForUpdate(String cacheName,Object key) {
		if (activeTx == null) {
			throw new IllegalStateException("No active transaction");
		}
		LogEntry entry = new LogEntry(cacheName, key, activeTx);
		txControl.put(entry, null);
	}

	public void markKeysForUpdate(String cacheName, Collection<?> keys) {
		if (activeTx == null) {
			throw new IllegalStateException("No active transaction");
		}
		Map<LogEntry, Void> buf =  new HashMap<LogEntry, Void>(keys.size());
		for(Object key : keys) {
			buf.put(new LogEntry(cacheName, key, activeTx), null);
		}
		txControl.putAll(buf);
	}
	
	public synchronized void commitWriteTx() {
		if (activeTx == null) {
			throw new IllegalStateException("No active transaction");
		}
		else {
			txControl.put(TX_COMMITED, activeTx);
			activeTx = null;
			txControl.unlock(TX_LOCK);
		}
	}
	
	// for internal use
	NamedCache getVersionedCache(String cacheName) {
		NamedCache cache = CacheFactory.getCache(cacheName);
		if (cache instanceof TxWrappedCache) {
			cache = ((TxWrappedCache)cache).getVersionedCache();
		}
		return cache;
	}
	
	public synchronized void rollbackWriteTx() {
		if (activeTx == null) {
			throw new IllegalStateException("No active transaction");
		}
		
		rollbackTxUpdates();
		Integer txc = (Integer) txControl.get(TX_COMMITED);
		txControl.put(TX_COUNTER, txc);
		activeTx =  null;
		txControl.unlock(TX_LOCK);
	}
	
	// for internal use
	@SuppressWarnings("unchecked")
	synchronized void rollbackTxUpdates() {
		if (activeTx == null) {
			throw new IllegalStateException("No active transaction");
		}
		
		Map<String, List<Object>> updateMap = new HashMap<String, List<Object>>();
		Map<String, List<LogEntry>> markerMap = new HashMap<String, List<LogEntry>>();
		for(Object key : txControl.keySet()) {
			if (key instanceof LogEntry) {
				LogEntry entry = (LogEntry) key;
				if (entry.getVersion() == activeTx) {
					List<Object> keys = updateMap.get(entry.getCacheName());
					if (keys == null) {
						keys = new ArrayList<Object>();
						updateMap.put(entry.getCacheName(), keys);
					}
					keys.add(entry.getKey());
					List<LogEntry> markers = markerMap.get(entry.getCacheName());
					if (markers == null) {
						markers = new ArrayList<LogEntry>();
						markerMap.put(entry.getCacheName(), markers);
					}
					markers.add(entry);
				}
			}
		}
		
		for(Map.Entry<String, List<Object>> entry :  updateMap.entrySet()) {
			NamedCache cache = getVersionedCache(entry.getKey());
			cache.invokeAll(entry.getValue(), new RollbackProcessor(activeTx));
			cache.keySet().removeAll(markerMap.get(entry.getKey()));
		}
		
		activeTx = null;
	}
	
	// for internal use
	@SuppressWarnings("unchecked")
	boolean isAlive(UID nodeId) {
		ServiceInfo si = txControl.getCacheService().getInfo();
		Set<Member> members = si.getServiceMembers();
		for(Member member: members) {
			if (member.getUid().equals(nodeId)) {
				return true;
			}
		}
		return false;
	}
	
	// for internal use
	@SuppressWarnings("unchecked")
	synchronized void cleanUpVersions(int batchLimit) {
		Set<UID> liveNodes = new HashSet<UID>();
		Set<UID> deadNodes = new HashSet<UID>();
		
		int minVersion = getLatestCommited();
		
		for(Object entry : new HashSet(txControl.keySet())) {
			if (entry instanceof ReaderLock) {
				ReaderLock lock = (ReaderLock)entry;
				boolean alive;
				if (deadNodes.contains(lock.getClientIndentity())) {
					alive = false;
				}
				else if (liveNodes.contains(lock.getClientIndentity())) {
					alive = true;
				}
				else if (isAlive(lock.getClientIndentity())) {
					liveNodes.add(lock.getClientIndentity());
					alive = true;
				}
				else {
					deadNodes.add(lock.getClientIndentity());
					alive = false;
				}
				if (!alive) {
					txControl.remove(lock);
				}
				else {
					if (minVersion > lock.getTxVersion()) {
						minVersion = lock.getTxVersion();
					}
				}
			}
		}
		
		// max in use version number is found 
	
		int counter = 0;
		Map<String, List<Object>> updateMap = new HashMap<String, List<Object>>();
		Map<String, List<LogEntry>> markerMap = new HashMap<String, List<LogEntry>>();
		for(Object key : txControl.keySet()) {
			if (key instanceof LogEntry) {
				LogEntry entry = (LogEntry) key;
				if (entry.getVersion() < minVersion) {
					++counter;
					List<Object> keys = updateMap.get(entry.getCacheName());
					if (keys == null) {
						keys = new ArrayList<Object>();
						updateMap.put(entry.getCacheName(), keys);
					}
					keys.add(entry.getKey());
					List<LogEntry> markers = markerMap.get(entry.getCacheName());
					if (markers == null) {
						markers = new ArrayList<LogEntry>();
						markerMap.put(entry.getCacheName(), markers);
					}
					markers.add(entry);
					
					if (counter > batchLimit) {
						break;
					}
				}
			}
		}
		
		for(Map.Entry<String, List<Object>> entry :  updateMap.entrySet()) {
			NamedCache cache = getVersionedCache(entry.getKey());
			cache.invokeAll(entry.getValue(), new RecycleProcessor(minVersion - 1));
			cache.keySet().removeAll(markerMap.get(entry.getKey()));
		}
	}
}