package org.gridkit.lab.meterx;

public interface EntityAppender extends HierarchyAppender {

	public EntityAppender addChildEntity(String entityId);		

	public ObserverAppender addChildObserver(String observerId);		
	
}
