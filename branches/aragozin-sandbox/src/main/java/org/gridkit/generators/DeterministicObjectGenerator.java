package org.gridkit.generators;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 * 
 * All generators are not thread safe!
 */
public interface DeterministicObjectGenerator<V> {
		
	public V object(long id);

	public DeterministicObjectGenerator<V> clone();
}
