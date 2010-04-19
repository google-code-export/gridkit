package com.griddynamics.coherence.integration.utils;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class AnyType {

	@SuppressWarnings("unchecked")
	public <T> T cast(Object o) {
		return (T)o;
	}	
}
