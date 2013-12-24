package org.gridkit.lab.meterx;

import java.util.Collection;

public interface EntityNode extends HierarchyNode {

	public Collection<EntityNode> childrenEntities();
	
	public Collection<ObserverNode> childrenObservers();
}
