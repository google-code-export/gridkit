package org.gridkit.lab.meterx;

public interface EntityBuilder {

	public EntityBuilder addChildEntity(String entityId);		

	public ObserverBuilder addChildObserver(String observerId, String observerClass);		
	
}
