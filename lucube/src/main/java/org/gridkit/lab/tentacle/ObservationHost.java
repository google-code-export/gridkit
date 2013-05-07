package org.gridkit.lab.tentacle;

public interface ObservationHost {

	public <S extends Sample> Observer<S> observer(Class<S> sample);
	
	public ObservationHost createChildHost();

	public <S extends Sample> void reportOnce();
	
	/**
	 * This method demarkates beginning of observable target's timespan.
	 */
	public void activate();

	/**
	 * This method demarkates end of observable target's timespan. 
	 */	
	public void destroy();	

}
