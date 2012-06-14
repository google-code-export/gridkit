package org.gridkit.util.vicontrol;

import java.util.LinkedHashMap;
import java.util.Map;

public class ViHostConfig implements ViConfigurable {

	private Map<String, String> props = new LinkedHashMap<String, String>();
	private Map<String, HookInfo> startupHooks = new LinkedHashMap<String, HookInfo>();
	private Map<String, HookInfo> shutdownHooks = new LinkedHashMap<String, HookInfo>();
	
	@Override
	public void setProp(String propName, String value) {
		props.put(propName, value);
	}

	@Override
	public void setProps(Map<String, String> props) {
		props.putAll(props);
	}

	@Override
	public void addStartupHook(String name, Runnable hook, boolean override) {
		if (startupHooks.containsKey(name) && !override) {
			throw new IllegalArgumentException("Startup hook '" + name + "' is already present");
		}
		startupHooks.put(name, new HookInfo(name, hook, override));
	}
	
	@Override
	public void addShutdownHook(String name, Runnable hook, boolean override) {
		if (shutdownHooks.containsKey(name) && !override) {
			throw new IllegalArgumentException("Shutdown hook '" + name + "' is already present");
		}
		shutdownHooks.put(name, new HookInfo(name, hook, override));
	}
	
	public void apply(ViConfigurable target) {
		target.setProps(props);
		for(HookInfo hi : startupHooks.values()) {
			target.addStartupHook(hi.name, hi.hook, hi.override);
		}
		for(HookInfo hi : shutdownHooks.values()) {
			target.addShutdownHook(hi.name, hi.hook, hi.override);
		}	
	}
	
	private static class HookInfo {
		
		public String name;
		public Runnable hook;
		public boolean override;
		
		public HookInfo(String name, Runnable hook, boolean override) {
			this.name = name;
			this.hook = hook;
			this.override = override;
		}
	}
}
