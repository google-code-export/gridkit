package org.gridkit.gatling.utils;

public class NoSpeedLimit implements SpeedLimit {

	@Override
	public void accure() {
		// do nothing
	}

	@Override
	public void dispose() {
		// do nothing
	}
}
