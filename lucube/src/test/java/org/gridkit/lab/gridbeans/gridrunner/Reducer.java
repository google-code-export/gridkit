package org.gridkit.lab.gridbeans.gridrunner;

/**
 * <p>
 * This a special interface for all/reduce type of distributed
 * coordination.
 * </p>
 * <p>
 * Process method can be called only once on each instance.
 * </p>
 * 
 * @author Alexey Ragzoin (alexey.ragozin@gmail.com)
 */
public interface Reducer<I, O> {

	/**
	 * Should be called only once.
	 */
	public O process(I input);
	
}
