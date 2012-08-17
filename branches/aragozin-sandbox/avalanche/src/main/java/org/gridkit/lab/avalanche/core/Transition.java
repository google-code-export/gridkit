package org.gridkit.lab.avalanche.core;

public interface Transition {

	public Component getComponent();
	
	public Knot getInitialState();
	
	public Knot getTargetState();
	
	public Transition getParentTransition();
	
	
}
