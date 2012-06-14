package org.gridkit.util.vicontrol;

import java.util.Map;

public interface ViConfigurable {

	public void setProp(String propName, String value);
	
	public void setProps(Map<String, String> props);
	
	public void addStartupHook(String name, Runnable hook, boolean override);

	public void addShutdownHook(String name, Runnable hook, boolean override);
	
}
