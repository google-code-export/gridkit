package org.gridkit.vicluster.spi;

import org.gridkit.util.concurrent.AdvancedExecutor;

public interface ViNodeSPI {
	
	public AdvancedExecutor getExecutor();
	
	public void applyPerc(ViPerc perc);

}
