package org.gridkit.coherence.profile;

import org.gridkit.coherence.profile.runtime.RuntimeStats;

/**
 * @author Dmitri Babaev
 */
public class StopWatch {
	private boolean stopped = false;
	private long startTime = RuntimeStats.nanoTime();
	
	public void stop(Sampler sampler) {
		if (stopped)
			throw new IllegalStateException("watch is already stopped");
		
		stopped = true;
		sampler.addSample(RuntimeStats.nanoTime() - startTime);
	}
}
