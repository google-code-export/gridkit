package org.gridkit.lab.tentacle;

import org.gridkit.util.concurrent.FutureEx;

public interface MonitoringActivity {

	public FutureEx<Void> start();
	
	public void join();
	
	public void stop();
	
}
