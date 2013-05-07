package org.gridkit.lab.tentacle;

import org.gridkit.util.concurrent.FutureEx;

public interface Locator<F extends MonitoringTarget, T extends MonitoringTarget, S extends Source<T>> {

	public LocationManager<F> newLocationManager();
	
	public boolean isCompatible(LocationManager<F> manager);	

	public interface LocationManager<F extends MonitoringTarget> {
		
		public void bind(F target);
		
		public <T extends MonitoringTarget> void addAction(Locator<F, T, ? extends Source<T>> locator, TargetAction<T> script);
		
		public void deploy();
		
		public FutureEx<Void> start();
		
		public void stop();
		
	}
	
	public interface TargetAction<T extends MonitoringTarget> {
		
		public TargetActivity deploy(T target);

	}

	public interface TargetActivity {
		
		public FutureEx<Void> start();
		
		public void stop();
		
	}
}
