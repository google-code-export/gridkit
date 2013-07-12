package org.gridkit.benchmark.gc;

import org.gridkit.lab.data.Sample;

public interface SweepStrategy {

	/**
	 * @return next data point to analyze or null
	 */
	public Sample nextDataPoint();
	
	/**
	 * Notifies strategy about calculate data point
	 */
	public void notifyResult(Sample sample);
	
}
