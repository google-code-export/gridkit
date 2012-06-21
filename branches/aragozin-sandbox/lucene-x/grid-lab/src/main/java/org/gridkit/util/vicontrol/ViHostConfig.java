/**
 * Copyright 2012 Alexey Ragozin
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
package org.gridkit.util.vicontrol;

import java.util.LinkedHashMap;
import java.util.Map;

public class ViHostConfig implements ViConfigurable {

	private Map<String, String> props = new LinkedHashMap<String, String>();
	private Map<String, HookInfo> startupHooks = new LinkedHashMap<String, HookInfo>();
	private Map<String, HookInfo> shutdownHooks = new LinkedHashMap<String, HookInfo>();
	
	public String getProp(String propName) {
		return props.get(propName);
	}

	public String getProp(String propName, String def) {
		String val = props.get(propName);
		return val == null ? def : val;
	}

	public Map<String, String> getAllProps(String prefix) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for(String key: props.keySet()) {
			if (key.startsWith(prefix)) {
				result.put(key, props.get(key));
			}			
		}
		return result;
	}
	
	@Override
	public void setProp(String propName, String value) {
		props.put(propName, value);
	}

	@Override
	public void setProps(Map<String, String> props) {
		this.props.putAll(props);
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
