package org.gridkit.litter.processing;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface LitterHandler {
	
	public Object produce(int size);
	
	public Object lump(Object piece1, Object piece2);

}
