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
package org.gridkit.coherence.util.arbiter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ConcurrentMap;

/**
 * Distributed HA resources connection manager.
 * Uses distributed lock in Coherence cache to ensure
 * connection availability to each resource.
 * 
 * Call start() method to enable live rebalancing.
 * 
 * @see FairShare
 * @see ResourceControl
 *
 * @author Alexey Ragozin (aragozin@griddynamics.com)
 */
public class DistributedResourceCoordinator {

	private static final Logger log = LoggerFactory.getLogger(DistributedResourceCoordinator.class);

	private ConcurrentMap controlCache;
	private ResourceControl manager;
	private FairShare fairShare;

	private int checkPeriod = 200;
	private int balancePeriodMillis = 10000;

	private int activeCount;
	private int standByCount;

	private volatile boolean stopped = false;

	private Map<Object, SourceControl> sources;

	private ControlThread thread;

	/**
	 * Starts live sources rebalancing among peers
	 */
	public void start() {
		// init sources
		initSourceControls();
		
		// start balancer
		thread = new ControlThread();
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stops live sources rebalancing
	 */
	public void stop() {
		try {
			stopped = true;
			thread.join();
		} catch (InterruptedException e) {
			log.error("Interrupted when waiting for {} to shutdown", thread.getName());
		}
	}

	private void initSourceControls() {
		sources = new HashMap<Object, SourceControl>();
		for(Object id: manager.getResourcesList()) {
			sources.put(id, new SourceControl(id));
		}
	}

	int getActiveCount() {
		return activeCount;
	}

	int getStandByCount() {
		return standByCount;
	}

	void printStatus() {
		List<Object> active = new ArrayList<Object>(sources.size());
		List<Object> standby = new ArrayList<Object>(sources.size());
		for(SourceControl control: sources.values()) {
			if (control.active) {
				active.add(control.sourceId);
			}
			if (control.standby) {
				standby.add(control.sourceId);
			}
		}
		System.out.println("Active " + active.toString() + ", standby " + standby.toString());
	}

	private void shutdown() {
		for(SourceControl control: sources.values()) {
			if (control.active) {
				manager.disconnect(control.sourceId);
				controlCache.unlock(activeKey(control));
			}
			if (control.standby) {
				controlCache.unlock(standbyKey(control));
			}
		}
		activeCount = 0;
		standByCount = 0;
	}

	private void checkLocks() {
		int fairSourceNumber = fairShare.getFairShare(sources.size());

		if (activeCount < fairSourceNumber) {
			// fast locking cycle
			for(SourceControl control: controls()) {
				if (!control.active) {
					tryLock(control);
				}
				if (activeCount >= fairSourceNumber) {
					break;
				}
			}
		}

		// acquire pending locks / slow locking cycle
		for(SourceControl control: controls()) {
			if (control.standby) {
				tryLock(control);
			}
		}

		// acquire stand by positions
		for(SourceControl control: controls()) {
			if (!control.active && !control.standby) {
				enterLine(control);
			}
		}
	}

	private void balance() {
		int fairSourceNumber = fairShare.getFairShare(sources.size());

		// release stand by slots
		for(SourceControl control: controls()) {

			if (standByCount <= fairSourceNumber) {
				break;
			}

			if (control.standby) {
				leaveLine(control);
			}
		}

		// deactivate active slots
		for(SourceControl control: controls()) {

			if (activeCount <= fairSourceNumber) {
				break;
			}

			if (control.active) {
				deactivate(control);
			}
		}
	}

	private void tryLock(SourceControl control) {
		if (control.active) {
			return;
		}

		boolean locked = controlCache.lock(activeKey(control), 1);
		if (locked) {
			log.info("Master lock acquired for " + control.sourceId);
			if (control.standby) {
				controlCache.unlock(standbyKey(control));
				--standByCount;
				log.info("StandBy lock released for " + control.sourceId);
			}
			control.active = true;
			control.standby = false;
			++activeCount;
			control.timestamp = System.currentTimeMillis();
			manager.connect(control.sourceId);
		}
	}

	private void deactivate(SourceControl control) {
		if (!control.active) {
			return;
		}

		boolean locked = controlCache.lock(standbyKey(control), 1);
		if (locked) {
			log.info("Cannot release master lock (no standby present) " + control.sourceId);
			// no stand by present, will not give up ownership
			controlCache.unlock(standbyKey(control));
		} else {
			log.info("Releasing master lock for " + control.sourceId);
			manager.disconnect(control.sourceId);
			controlCache.unlock(activeKey(control));
			control.active = false;
			--activeCount;
			log.info("Master lock released for " + control.sourceId);
		}
	}

	private void enterLine(SourceControl control) {
		if (control.active || control.standby) {
			return;
		}

		boolean locked = controlCache.lock(standbyKey(control), 1);
		if (locked) {
			log.info("StandBy lock acquired for " + control.sourceId);
			++standByCount;
			control.standby = true;
			control.timestamp = System.currentTimeMillis();
		}
	}

	private void leaveLine(SourceControl control) {
		if (!control.standby) {
			return;
		}

		boolean locked = controlCache.lock(activeKey(control), 1);
		if (locked) {
			log.info("StandBy lock cannot be released (no owner) " + control.sourceId);
			// not active owner, will not leave line
			controlCache.unlock(activeKey(control));
		} else {
			controlCache.unlock(standbyKey(control));
			control.standby = false;
			--standByCount;
			log.info("StandBy lock released for " + control.sourceId);
		}
	}

	private ResourceLockKey activeKey(SourceControl control) {
		return new ResourceLockKey(ResourceLockKey.KEYTYPE_ACTIVE, control.sourceId);
	}

	private ResourceLockKey standbyKey(SourceControl control) {
		return new ResourceLockKey(ResourceLockKey.KEYTYPE_STANDBY, control.sourceId);
	}

	private List<SourceControl> controls() {
		List<SourceControl> controls = new ArrayList<SourceControl>(sources.values());
		Collections.sort(controls, new SourceControlComparator());
		return controls;
	}

	private void sleepUntil(long until) {
		while(until > System.currentTimeMillis()) {
			long sleepTime = until - System.currentTimeMillis();
			if (sleepTime <= 0) {
				break;
			}
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(sleepTime));
		}
	}

	public void setFairShare(FairShare fairShare) {
		this.fairShare = fairShare;
	}

	public void setLockCheckPeriodMillis(int period) {
		checkPeriod = period;
	}

	public void setRebalancePeriodMillis(int period) {
		balancePeriodMillis = period;
	}

	public void setLockMap(ConcurrentMap cache) {
		this.controlCache = cache;
	}

	public void setDatasyncManager(ResourceControl manager) {
		this.manager = manager;
	}

	
	
	
	private static class SourceControl {

		Object sourceId;
		boolean active;
		boolean standby;
		long timestamp;

		public SourceControl(Object ref) {
			this.sourceId = ref;
		}
	}

	
	
	
	public class ControlThread extends Thread {

		public ControlThread() {
			setName("DistributedResourceCoordinator");
		}

		@Override
		public void run() {
			long lastBalance = System.currentTimeMillis();

			while(true) {
				if (stopped) {
					shutdown();
					return;
				}

				long lastCheck = System.currentTimeMillis();

				checkLocks();
				if (lastBalance + balancePeriodMillis < System.currentTimeMillis()) {
					lastBalance = System.currentTimeMillis();
					balance();
				}

				sleepUntil(lastCheck + checkPeriod);
			}
		}
	}
	
	
	
	
	
	private static class SourceControlComparator implements Comparator<SourceControl> {
		@Override
		public int compare(SourceControl o1, SourceControl o2) {
			return (int)(o1.timestamp - o2.timestamp);
		}
	}
	
	
	
	public static class ResourceLockKey implements Serializable, PortableObject {
		
		private static final long serialVersionUID = -4331553392611241865L;
		
		public static final String KEYTYPE_ACTIVE  = "ACTIVE";
		public static final String KEYTYPE_STANDBY = "STANDBY";
		
		private Object resourceId;
		private String keyType;
		
		@SuppressWarnings("unused")
		private ResourceLockKey() {
			//for serialization
		}
		
		public ResourceLockKey(String keyType, Object resourceId) {
			this.resourceId = resourceId;
			this.keyType = keyType;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((keyType == null) ? 0 : keyType.hashCode());
			result = prime * result
					+ ((resourceId == null) ? 0 : resourceId.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ResourceLockKey other = (ResourceLockKey) obj;
			if (keyType == null) {
				if (other.keyType != null)
					return false;
			} else if (!keyType.equals(other.keyType))
				return false;
			if (resourceId == null) {
				if (other.resourceId != null)
					return false;
			} else if (!resourceId.equals(other.resourceId))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LockKey-").append(keyType).append("[").append(resourceId).append("]");
			return builder.toString();
		}

		@Override
		public void readExternal(PofReader pofReader) throws IOException {
			keyType = pofReader.readString(0);
			resourceId = pofReader.readObject(1);
		}

		@Override
		public void writeExternal(PofWriter pofWriter) throws IOException {
			pofWriter.writeString(0, keyType);
			pofWriter.writeObject(1, resourceId);
		}
	}
	
}
