package org.gridkit.lab.tentacle;

import java.util.List;

import org.gridkit.lab.tentacle.Locator.LocationManager;
import org.gridkit.lab.tentacle.Metrics.Hostname;

public interface ActiveNode extends MonitoringTarget {

	public static final ActiveNodeLocator ALL = new ActiveNodeLocator();

	public static final Metric<Hostname, ActiveNode> HOSTNAME = null;
	
	public static class ActiveNodeLocator implements Locator<DistributedExperiment, ActiveNode, ActiveNodeSource>, ActiveNodeFilter<ActiveNodeLocator> {

		private List<String[]> filters;
		
		public ActiveNodeLocator(String filter) {
			
		}
		
		@Override
		public ActiveNodeLocator filter(String pattern) {
			throw new UnsupportedOperationException();
		}

		@Override
		public LocationManager<DistributedExperiment> newLocationManager() {
			return null;
		}

		@Override
		public boolean isCompatible(
				LocationManager<DistributedExperiment> manager) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public interface ActiveNodeFilter<X> extends LocationFilterable<X> {
		
		public X filter(String pattern);
		
	}
	
	public interface ActiveNodeSource extends Source<ActiveNode>, Observable<ActiveNodeSource, ActiveNode>, ActiveNodeFilter<ActiveNodeSource> {
	
		public ActiveNodeSource filter(String pattern);
		
	}
	
	private static class ActiveNodeManager implements LocationManager<MonitoringTarget>
	
}
