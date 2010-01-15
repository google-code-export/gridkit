package com.griddynamics.gridkit.coherence.patterns.command.internal;


public class ContextMetaKey extends ContextedKey {

	private static final long serialVersionUID = 20100109L;
	
	public ContextMetaKey() {
		// for POF
	}
	
	public ContextMetaKey(Object contextKey) {
		this.contextKey = contextKey;
	}
}
