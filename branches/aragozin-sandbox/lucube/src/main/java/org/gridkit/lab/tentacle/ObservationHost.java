package org.gridkit.lab.tentacle;

public interface ObservationHost {

	public <S extends Sample> Observer<S> observer(Class<? extends S> sample);
	
	public ObservationHost createChildHost();
	
	public void addActivity(ObservationActivity activity);
	
	/**
	 * This method demarkates end of observable target's timespan.
	 * It will also recursively destroy all downstream nodes. 
	 */	
	public void destroy();	

	public void reportError(String message, Throwable error);

	public interface ObservationActivity {
		
		public void start();
		
		public void stop();
		
	}
}
