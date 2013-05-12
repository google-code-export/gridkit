package org.gridkit.lab.tentacle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.gridkit.lab.jvm.attach.AttachManager;
import org.gridkit.lab.jvm.attach.JavaProcessId;
import org.gridkit.lab.tentacle.LocalJvmProcess.LocalJvmProcessFilter;
import org.gridkit.lab.tentacle.LocalJvmProcess.LocalJvmProcessSource;
import org.gridkit.util.concurrent.TaskService;

class LocalJvmProcessLocator implements Locator<ActiveNode, LocalJvmProcess, LocalJvmProcessSource>, LocalJvmProcessFilter {

	@Override
	public boolean evaluate(LocalJvmProcess process) {
		return true;
	}

	@Override
	public LocationManager<ActiveNode> newLocationManager() {
		return new LocalJvmFinder();
	}

	@Override
	public boolean isCompatible(LocationManager<ActiveNode> manager) {
		return manager instanceof LocalJvmFinder;
	}

	private static class LocalJvmFinder extends CommonLocationManager<ActiveNode> {

		@Override
		protected void bound() {
			for(JavaProcessId jpid: AttachManager.listJavaProcesses()) {
				addSubnode(new LocalJvmSubnode(jpid.getPID()));
			}
		}
		
		
		private class LocalJvmSubnode implements Subnode<ActiveNode> {
			
			private long pid;
			
			private JvmTarget target;
			
			public LocalJvmSubnode(long pid) {
				this.pid = pid;
			}
			
			@Override
			public boolean evaluate(Locator<ActiveNode, ?, ?> locator) {
				if (locator instanceof LocalJvmProcessFilter) {
					return ((LocalJvmProcessFilter)locator).evaluate(new FilterableJvmProces(pid));
				}
				else {
					throw new IllegalArgumentException("Unsupported locator: " + locator);
				}
			}
			
			@Override
			public synchronized void deploy(TargetAction<?> script) {
				if (target == null) {
					String hostname;
					try {
						hostname = InetAddress.getLocalHost().getHostName();
					} catch (UnknownHostException e) {
						hostname = "<unknown>";
					}
					ObservationHost proch = getTarget().getObservationHost().createChildHost(Samples.process(hostname, String.valueOf(pid)));
					target = new JvmTarget(pid, proch, getTarget().getSharedTaskService());
				}
				script.apply(target);
			}
		}
	}
	
	private static class JvmTarget extends BasicMonitoringTarget implements LocalJvmProcess {
		
		private long pid;
		
		public JvmTarget(long pid, ObservationHost host, TaskService sharedScheduler) {
			super(host, sharedScheduler);
			this.pid = pid;
		}
		
		@Override
		public long getPid() {
			return pid;
		}
		
		@Override
		public String getSystemProperty(String prop) {
			return AttachManager.getDetails(pid).getSystemProperties().getProperty(prop);
		}

		@Override
		public Map<String, String> getSystemProperties(Collection<String> props) {
			Map<String, String> result = new LinkedHashMap<String, String>();
			Properties sp = AttachManager.getDetails(pid).getSystemProperties();
			for(String prop: props) {
				if (sp.containsKey(prop)) {
					result.put(prop, (String) sp.get(prop));
				}
			}
			return result;
		}
	}
	
	private static class FilterableJvmProces implements LocalJvmProcess {
		
		private long pid; 
		
		public FilterableJvmProces(long pid) {
			this.pid = pid;
		}
		
		@Override
		public long getPid() {
			return pid;
		}
	
		@Override
		public String getSystemProperty(String prop) {
			return AttachManager.getDetails(pid).getSystemProperties().getProperty(prop);
		}

		@Override
		public Map<String, String> getSystemProperties(Collection<String> props) {
			Map<String, String> result = new LinkedHashMap<String, String>();
			Properties sp = AttachManager.getDetails(pid).getSystemProperties();
			for(String prop: props) {
				if (sp.containsKey(prop)) {
					result.put(prop, (String) sp.get(prop));
				}
			}
			return result;
		}

		@Override
		public TaskService getSharedTaskService() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public TaskService getBoundTaskService() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public ObservationHost getObservationHost() {
			throw new UnsupportedOperationException();
		}
	}
}
