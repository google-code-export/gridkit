package com.oracle.coherence.patterns.command;

import java.io.Serializable;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>A {@link ContextConfiguration} defines the properties that specify
 * how a {@link Context} is managed, together with the expected semantics 
 * of the environment in which the {@link Context} is operating.</p>
 * 
 * <p>As {@link ContextConfiguration}s are cached objects (ie: placed in Coherence caches), 
 * they need to at least implement {@link Serializable}, or better still, implement
 * {@link ExternalizableLite} or {@link PortableObject}.</p> 
 * 
 * @author Brian Oliver
 */
public interface ContextConfiguration {

	/**
	 * <p>The {@link ManagementStrategy} of a {@link Context} 
	 * is used to specify how work (like {@link Command}s) are
	 * stored and managed.</p> 
	 */
	public enum ManagementStrategy {
		
		/**
		 * <p>The <code>COLOCATED</code> strategy ensures that
		 * all work (like {@link Command}s) are managed in the 
		 * JVM where the {@link Context} is being managed.</p>
		 * 
		 * <p>This has the benefit of minimizing network traffic to
		 * execute work (meaning higher-performance), but means more 
		 * JVM tuning needs to occur ensure all work (like {@link Command}s)
		 * can co-exist in the same JVM with the {@link Context}.</p>
		 */
		COLOCATED,
		
		/**
		 * <p>The <code>DISTRIBUTED</code> strategy ensures that
		 * all work (like {@link Command}s) are stored and
		 * managed in a distributed manner across the available 
		 * resources (ie: clustered JVMs) instead of being "colocated".</p>
		 * 
		 * <p>This has the benefit of increasing the amount of work 
		 * that may be submitted for execution (meaning greater capacity),
		 * but also means lower performance as it introduces more
		 * network traffic between JVMs.</p> 
		 */
		DISTRIBUTED;
	}
	
	
	/**
	 * <p>Returns the {@link ManagementStrategy} for the 
	 * {@link Context} being configured.</p>
	 */
	public ManagementStrategy getManagementStrategy();
	
}
