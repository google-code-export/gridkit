package org.gridkit.lab.gridbeans.gridrunner;

public interface Site {

	public <T> T deploy(String key, Class<T> type);
	
	
	
}
