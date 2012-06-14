package org.gridkit.util.vicontrol;


/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 */
public interface ViHost extends ViExecutor, ViConfigurable {

	public void suspend();
	
	public void resume();
	
	public void shutdown();

	public void kill();
	
}
