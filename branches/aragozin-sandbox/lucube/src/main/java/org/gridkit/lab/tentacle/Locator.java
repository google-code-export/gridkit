package org.gridkit.lab.tentacle;


public interface Locator<F extends MonitoringTarget, T extends MonitoringTarget, S extends Source<T>> {

	public LocationManager<F> newLocationManager();
	
	public boolean isCompatible(LocationManager<F> manager);	

	public interface LocationManager<F extends MonitoringTarget> {
		
		public void bind(F target);
		
		public <T extends MonitoringTarget> void addAction(Locator<F, T, ? extends Source<T>> locator, TargetAction<T> script);
		
		/**
		 * Called after last {@link #addAction(Locator, TargetAction)} call.
		 */
		public void deploy();
		
	}
	
	/**
	 * {@link TargetAction} encapsulates downstream configuration related to this node.
	 * {@link Locator} is expected to stack all actions for given target instance together and apply once.
	 * It would allow exploit deployment optimizations. 
	 */
	public interface TargetAction<T extends MonitoringTarget> {
		
		/**
		 * Generic constraints are relaxed to avoid casting. It is not meant to be used to used with bound generic types anyway.
		 */
		public TargetAction<T> stack(TargetAction<?> other);
		
		public void apply(T target);

	}
}
