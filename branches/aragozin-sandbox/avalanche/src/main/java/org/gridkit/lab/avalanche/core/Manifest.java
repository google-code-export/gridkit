package org.gridkit.lab.avalanche.core;

import java.util.Collection;

public interface Manifest {

	public Component getComponent();
	
	public Collection<Knot> getKnots();
	
	public Transition getTransition(Knot from, Knot to);
	
	public Collection<Dependency> getDependencies(Knot knot);
}
