package org.gridkit.lab.avalanche.core;

public interface ComponentState {
	
	public Knot getCurrentState();
	
	public TransitionState getCurrentTransition();

}
