package org.gridkit.lab.tentacle;

import java.io.Serializable;

import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeFilter;
import org.gridkit.lab.tentacle.ActiveNode.ActiveNodeSource;

abstract class ActiveNodeLocator implements Locator<DistributedExperiment, ActiveNode, ActiveNodeSource>, ActiveNodeFilter<ActiveNodeLocator>, Serializable {

	protected abstract boolean evaluate(String name);
	
	@Override
	public ActiveNodeLocator filter(String pattern) {
		return new MatchLocator(this, pattern);
	}

	@Override
	public LocationManager<DistributedExperiment> newLocationManager() {
		return new ActiveLocationManager();
	}

	@Override
	public boolean isCompatible(LocationManager<DistributedExperiment> manager) {
		return manager instanceof ActiveLocationManager;
	}
	
	static class AllLocator extends ActiveNodeLocator {
		@Override
		protected boolean evaluate(String name) {
			return true;
		}
	}

	static class MatchLocator extends ActiveNodeLocator {
		
		private final ActiveNodeLocator prev;
		private final String[] patterns;
		
		public MatchLocator(ActiveNodeLocator prev, String... patterns) {
			this.prev = prev;
			this.patterns = patterns;
		}

		@Override
		protected boolean evaluate(String name) {
			if (!prev.evaluate(name)) {
				return false;
			}
			for(String pattern: patterns) {
				if (GlobHelper.translate(pattern, ".").matcher(name).matches()) {
					return true;
				}
			}
			return false;
		}
	}
}