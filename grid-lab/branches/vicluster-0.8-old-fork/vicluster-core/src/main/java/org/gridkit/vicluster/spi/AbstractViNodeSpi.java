package org.gridkit.vicluster.spi;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.util.concurrent.AdvancedExecutor;

public abstract class AbstractViNodeSpi implements ViNodeSpi {

	private List<ViNodeAction> preShutdownListeners;
	private List<ViNodeAction> postShutdownListeners;
	
	protected volatile boolean terminating;
	protected boolean terminated;
	
	@Override
	public <V> V adapt(Class<V> type) {
		if (type.isInstance(this)) {
			return type.cast(this);
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isActive() {
		return terminating;
	}
	
	protected void postInit(AttrBag config) {
		// TODO
	}
	
	@Override
	public abstract AdvancedExecutor getExecutor();

	@Override
	public synchronized void addPreShutdownAction(ViNodeAction action) {
		if (terminating) {
			throw new IllegalStateException("Shutdown");
		}
		if (preShutdownListeners == null) {
			preShutdownListeners = new ArrayList<ViNodeAction>();
		}
		preShutdownListeners.add(action);		
	}
	
	@Override
	public synchronized void addPostShutdownAction(ViNodeAction action) {
		if (terminating) {
			throw new IllegalStateException("Shutdown");
		}
		if (postShutdownListeners == null) {
			postShutdownListeners = new ArrayList<ViNodeAction>();
		}
		postShutdownListeners.add(action);		
	}
	
	@Override
	public void shutdown() {
		synchronized (this) {
			if (terminating) {
				return;
			}
			terminating = true;
		}
		if (preShutdownListeners != null) {
			for(ViNodeAction action: preShutdownListeners) {
				action.apply(this);
			}
		}
		destroy();
		if (postShutdownListeners != null) {
			for(ViNodeAction action: postShutdownListeners) {
				action.apply(this);
			}
		}
	}
	
	protected abstract void destroy();
}
