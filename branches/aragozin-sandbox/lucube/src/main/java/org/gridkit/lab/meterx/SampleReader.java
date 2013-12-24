package org.gridkit.lab.meterx;


public interface SampleReader {

	public ObserverNode getObserverNode();

	public boolean isReady();
	
	public boolean next();
	
	public Object get(String field);

	public boolean getBoolean(String field);

	public long getLong(String field);

	public double getDouble(String field);

	public String getString(String field);
}
