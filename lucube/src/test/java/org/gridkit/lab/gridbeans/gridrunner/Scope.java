package org.gridkit.lab.gridbeans.gridrunner;

public interface Scope {

	public <T> T deploy(String key, Class<T> type);
	
	
	
}
