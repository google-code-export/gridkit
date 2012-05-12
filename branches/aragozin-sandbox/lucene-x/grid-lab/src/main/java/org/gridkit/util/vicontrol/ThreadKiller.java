package org.gridkit.util.vicontrol;

/**
 * JVM does not provide means to kill a thread. This makes uncooperative termination of isolate problematic.
 * Thread killer is pluging using application specific means to kill thread remaining from isolate. 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ThreadKiller {

	/**
	 * @return <code>true</code> if killer have done any actions to thread 
	 */
	public boolean tryToKill(Thread thread);
	
}
