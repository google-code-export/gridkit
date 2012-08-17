package org.gridkit.lab.avalanche.core;

import java.util.Collection;
import java.util.Map;

public interface Component extends AttrMap {

	public Schema getSchema();
	
	public Manifest getManifest();
	
	public Component getOwner();
	
	public Knot getCurrentState();
	
	public Collection<Transition> currentTransitions();
	
	public Map<String, Component> getNamedSubComponents();
	
	public Collection<Component> getAllSubComponents();
	
	public <V> V get(AttrKey<V> key);
	
}
