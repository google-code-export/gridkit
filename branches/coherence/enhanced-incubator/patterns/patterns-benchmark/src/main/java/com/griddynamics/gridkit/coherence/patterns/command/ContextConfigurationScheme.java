package com.griddynamics.gridkit.coherence.patterns.command;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public interface ContextConfigurationScheme {

	public String getSchemeName();

	public String getContextCacheName();

	public String getCommandCacheName();
	
	public ManagementStrategy getCommandsPlacementStrategy();
	
	public boolean areCommandsCollocated();
	
	public int getThreadPoolSize();

	public static class DefaultContextConfigurationScheme implements ExternalizableLite, PortableObject, ContextConfigurationScheme{
		
		private static String DEFAULT_CONTEXT_CACHE_NAME = "enhanced-command-pattern.commands";
		private static String DEFAULT_THREAD_POOL_SIZE = "4";
		
		private static String PROP_SCHEME_NAME = "scheme-name"; 
		private static String PROP_STORAGE_STRATEGY = "storage-strategy"; 
		private static String PROP_THREAD_POOL_SIZE = "thread-pool-size";
		private static String PROP_CONTEXT_CACHENAME = "context-cache-name";
		private static String PROP_COMMAND_CACHENAME = "command-cache-name";
		
		private Map<String, String> props = new LinkedHashMap<String, String>();
		
		private boolean readonly = false;
		
		public DefaultContextConfigurationScheme() {
			// for POF
		}

		public DefaultContextConfigurationScheme(String schemeName, DefaultContextConfigurationScheme scheme) {
			props = new LinkedHashMap<String, String>(scheme.props);
			props.put(PROP_SCHEME_NAME, schemeName);
			readonly = true;
		}

		@Override
		public String getSchemeName() {
			return props.get(PROP_SCHEME_NAME);
		}

		@Override
		public String getContextCacheName() {
			String value = props.get(PROP_CONTEXT_CACHENAME);
			return value != null ? value : DEFAULT_CONTEXT_CACHE_NAME;
		}

		public void setContextCacheName(String cacheName) {
			if (readonly) {
				throw new IllegalStateException("Immutable instance");
			}
			props.put(PROP_CONTEXT_CACHENAME, cacheName);
		}
		
		@Override
		public String getCommandCacheName() {
			String value = props.get(PROP_COMMAND_CACHENAME);
			value = value != null ? value : props.get(PROP_CONTEXT_CACHENAME);
			return value != null ? value : DEFAULT_CONTEXT_CACHE_NAME;
		}

		public void setCommandCacheName(String cacheName) {
			if (readonly) {
				throw new IllegalStateException("Immutable instance");
			}			
			props.put(PROP_COMMAND_CACHENAME, cacheName);
		}
		
		@Override
		public ManagementStrategy getCommandsPlacementStrategy() {
			String value = props.get(PROP_STORAGE_STRATEGY);
			value = value != null ? value : ManagementStrategy.COLOCATED.name();
			return ManagementStrategy.valueOf(value);
		}

		public void setCommandPlacementStrategy(ManagementStrategy strategy) {
			if (readonly) {
				throw new IllegalStateException("Immutable instance");
			}			
			props.put(PROP_STORAGE_STRATEGY, strategy.name());
		}
		
		@Override
		public int getThreadPoolSize() {
			String value = props.get(PROP_THREAD_POOL_SIZE);
			value = value != null ? value : DEFAULT_THREAD_POOL_SIZE;
			return Integer.parseInt(value);
		}

		public void setThreadPoolSize(int threadPoolSize) {
			if (readonly) {
				throw new IllegalStateException("Immutable instance");
			}			
			props.put(PROP_THREAD_POOL_SIZE, String.valueOf(threadPoolSize));
		}
		
		@Override
		public boolean areCommandsCollocated() {
			return ManagementStrategy.COLOCATED.equals(getCommandsPlacementStrategy());
		}

		@Override
		public void readExternal(PofReader in) throws IOException {
			int propId = 0;
			while(true) {
				String key = in.readString(propId++);
				if (key.length() == 0) {
					break;
				}
				String value = in.readString(propId++);
				props.put(key, value);
			}
		}

		@Override
		public void writeExternal(PofWriter out) throws IOException {
			int propId = 0;
			for(Map.Entry<String, String> entry: props.entrySet()) {
				out.writeString(propId++, entry.getKey());
				out.writeString(propId++, entry.getValue());
			}
			out.writeString(propId++, "");
		}

		@Override
		public void readExternal(DataInput in) throws IOException {
			while(true) {
				String key = in.readUTF();
				if (key.length() == 0) {
					break;
				}
				String value = in.readUTF();
				props.put(key, value);
			}
		}

		@Override
		public void writeExternal(DataOutput out) throws IOException {
			for(Map.Entry<String, String> entry: props.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeUTF(entry.getValue());
			}
			out.writeUTF("");
		}
	}
}
