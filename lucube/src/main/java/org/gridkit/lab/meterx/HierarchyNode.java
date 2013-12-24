package org.gridkit.lab.meterx;

import java.util.Map;
import java.util.Set;

public interface HierarchyNode {

	public String id();
	
	public EntityNode getParent();
	
	public Map<String, String> getAttributes();
	
	public Set<String> getTraits();

}
