package org.gridkit.util.vicontrol;

import java.util.Map;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 */
public interface ViHost extends ViExecutor {

	public void setProp(String propName, String value);
	
	public void setProps(Map<String, String> props);
	
	public void addStartupHook(String name, Runnable initialzer, boolean override);

	public void addShutdownHook(String name, Runnable initialzer, boolean override);
	
	public void start();
	
	public void suspend();
	
	public void resume();
	
	public void stop();	
	
	public void shutdown();

	public void kill();
	
}
