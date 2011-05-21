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

import com.tangosol.util.ConcurrentMap;

/**
 * Distributed HA resources connection manager.
 * Uses distributed lock in Coherence cache to ensure
 * connection availability to each resource.
 * 
 * Call start() method to enable live rebalancing.
 * 
 * @see FairShareCalculator
 * @see ResourceHandler
 *
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DistributedResourceCoordinator {

	private static final Logger log = LoggerFactory.getLogger(DistributedResourceCoordinator.class);

	private ConcurrentMap controlCache;
	private ResourceHandler resourceHandler;
	private FairShareCalculator fairShare = new RoleBasedFairShare(); // default strategy

	private int checkPeriod = 200;
	private int balancePeriodMillis = 10000;

	private int activeCount;
	private int standByCount;

	private volatile boolean stopped = false;

	private Map<Object, SourceControl> sources;

	private ControlThread thread;

	public void setFairShare(FairShareCalculator fairShare) {
		this.fairShare = fairShare;
	}

	public void setLockCheckPeriodMillis(int period) {
		checkPeriod = period;
	}

	public void setRebalancePeriodMillis(int period) {
		balancePeriodMillis = period;
	}

	/**
	 * <b>Required</b>
	 */
	public void setLockMap(ConcurrentMap cache) {
		this.controlCache = cache;
	}

	/**
	 * <b>Required</b>
	 */
	public void setResourceHandler(ResourceHandler handler) {
		this.resourceHandler = handler;
	}

	/**
	 * Starts coordinator, once started coordinator will participate in resource balancing.
	 */
	public void start() {
		if (controlCache == null) {
			throw new IllegalStateException("Control map is not set!");
		}
		if (resourceHandler == null) {
			throw new IllegalStateException("Resource handler is not set!");
		}
		// init sources
		initSourceControls();
		
		// start balancer
		thread = new ControlThread();
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Stops active sources, release locks and stops watch dog thread.
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
		for(Object id: resourceHandler.getResourcesList()) {
			sources.put(id, new SourceControl(id));
		}
	}

	int getActiveCount() {
		return activeCount;
	}

	int getStandByCount() {
		return standByCount;
	}

	/* for debug, called only from tests */
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
				resourceHandler.disconnect(control.sourceId);
				controlCache.unlock(activeKey(control.sourceId));
			}
			if (control.standby) {
				controlCache.unlock(standbyKey(control.sourceId));
			}
		}
		activeCount = 0;
		standByCount = 0;
	}

	/**
	 * Quick check. Only resource acquisition is performed.
	 */
	private void greedyBalance() {

		int fairSourceNumber = fairShare.getFairShare(sources.size());

		if (activeCount < fairSourceNumber) {
			// fast locking cycle
			for(SourceControl control: controls()) {
				if (!control.active) {
					requestOwnership(control);
				}
				if (activeCount >= fairSourceNumber) {
					break;
				}
			}
		}

		// acquire pending locks / slow locking cycle
		for(SourceControl control: controls()) {
			if (control.standby) {
				requestOwnership(control);
			}
		}

		// acquire stand by positions
		for(SourceControl control: controls()) {
			if (!control.active && !control.standby) {
				enterLine(control);
			}
		}
	}

	private void fairBalance() {
		
		int fairSourceNumber = fairShare.getFairShare(sources.size());

		// release stand by slots
		for(SourceControl control: controls()) {

			if ((standByCount + activeCount) <= fairSourceNumber) {
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
				withdrawOwnership(control);
			}
		}
	}

	private void requestOwnership(SourceControl control) {
		
		if (control.active) {
			return;
		}

		boolean locked = controlCache.lock(activeKey(control.sourceId), 1);
		if (locked) {
			log.info("Master lock acquired for [" + control.sourceId + "]");
			if (control.standby) {
				controlCache.unlock(standbyKey(control.sourceId));
				--standByCount;
				log.info("Stand by lock released for [" + control.sourceId + "]");
			}
			control.active = true;
			control.standby = false;
			++activeCount;
			control.timestamp = System.currentTimeMillis();
			try {
				resourceHandler.connect(control.sourceId);
			}
			catch(Exception e) {
				log.error("Exception in ResourceControl.connect for resource [" + control.sourceId + "]" , e);
				log.error("Releasing master lock for [" + control.sourceId + "]");
				--activeCount;
				controlCache.unlock(activeKey(control.sourceId));
				control.active = false;
				control.standby = false;
			}
		}
	}

	private void withdrawOwnership(SourceControl control) {
		
		if (!control.active) {
			return;
		}

		boolean locked = controlCache.lock(standbyKey(control.sourceId), 1);
		if (locked) {
			log.info("Cannot release master lock (no standby present) " + control.sourceId);
			// no stand by present, will not give up ownership
			controlCache.unlock(standbyKey(control.sourceId));
		} else {
			log.info("Releasing master lock for " + control.sourceId);
			try {
				resourceHandler.disconnect(control.sourceId);
			}
			catch(Exception e) {
				log.error("Exception in ResourceControl.disconnect for resource [" + control.sourceId + "]" , e);
			}
			controlCache.unlock(activeKey(control.sourceId));
			control.active = false;
			--activeCount;
			log.info("Master lock released for " + control.sourceId);
		}
	}

	private void enterLine(SourceControl control) {
		
		if (control.active || control.standby) {
			return;
		}

		boolean locked = controlCache.lock(standbyKey(control.sourceId), 1);
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

		boolean locked = controlCache.lock(activeKey(control.sourceId), 1);
		if (locked) {
			log.info("StandBy lock cannot be released (no owner) " + control.sourceId);
			// not active owner, will not leave line
			controlCache.unlock(activeKey(control.sourceId));
		} else {
			controlCache.unlock(standbyKey(control.sourceId));
			control.standby = false;
			--standByCount;
			log.info("StandBy lock released for " + control.sourceId);
		}
	}

	// package visibility for test access
	static Object activeKey(Object sourceId) {
		// ArrayList is used to use default serialization
		List<Object> key = new ArrayList<Object>(2);
		key.add("active");
		key.add(sourceId);
		return key;
	}

	// package visibility for test access
	static Object standbyKey(Object sourceId) {
		List<Object> key = new ArrayList<Object>(2);
		key.add("stand-by");
		key.add(sourceId);
		return key;
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
			long lastFairBalance = System.currentTimeMillis();

			while(true) {
				if (stopped) {
					shutdown();
					return;
				}

				long lastCheck = System.currentTimeMillis();

				greedyBalance();
				if (lastFairBalance + balancePeriodMillis < System.currentTimeMillis()) {
					lastFairBalance = System.currentTimeMillis();
					fairBalance();
				}

				sleepUntil(lastCheck + checkPeriod);
			}
		}
	}
	
	/**
	 * Comparator to sort SourceControls by timestamps (from older to newer).
	 */
	private static class SourceControlComparator implements Comparator<SourceControl> {
		@Override
		public int compare(SourceControl o1, SourceControl o2) {
			return (int)(o1.timestamp - o2.timestamp);
		}
	}
	
//	public static class ResourceLockKey implements Serializable, PortableObject {
//		
//		private static final long serialVersionUID = -4331553392611241865L;
//		
//		public static final String KEYTYPE_ACTIVE  = "ACTIVE";
//		public static final String KEYTYPE_STANDBY = "STANDBY";
//		
//		private Object resourceId;
//		private String keyType;
//		
//		@SuppressWarnings("unused")
//		private ResourceLockKey() {
//			//for serialization
//		}
//		
//		public ResourceLockKey(String keyType, Object resourceId) {
//			this.resourceId = resourceId;
//			this.keyType = keyType;
//		}
//		
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result
//					+ ((keyType == null) ? 0 : keyType.hashCode());
//			result = prime * result
//					+ ((resourceId == null) ? 0 : resourceId.hashCode());
//			return result;
//		}
//		
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			ResourceLockKey other = (ResourceLockKey) obj;
//			if (keyType == null) {
//				if (other.keyType != null)
//					return false;
//			} else if (!keyType.equals(other.keyType))
//				return false;
//			if (resourceId == null) {
//				if (other.resourceId != null)
//					return false;
//			} else if (!resourceId.equals(other.resourceId))
//				return false;
//			return true;
//		}
//		
//		@Override
//		public String toString() {
//			StringBuilder builder = new StringBuilder();
//			builder.append("LockKey-").append(keyType).append("[").append(resourceId).append("]");
//			return builder.toString();
//		}
//
//		@Override
//		public void readExternal(PofReader pofReader) throws IOException {
//			keyType = pofReader.readString(0);
//			resourceId = pofReader.readObject(1);
//		}
//
//		@Override
//		public void writeExternal(PofWriter pofWriter) throws IOException {
//			pofWriter.writeString(0, keyType);
//			pofWriter.writeObject(1, resourceId);
//		}
//	}
}
