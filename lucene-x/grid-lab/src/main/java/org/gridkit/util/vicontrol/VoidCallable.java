package org.gridkit.util.vicontrol;

import java.util.concurrent.Callable;

public interface VoidCallable {		
	public static class VoidCallableWrapper implements Callable<Void> {
		
		public final VoidCallable callable;
		
		public VoidCallableWrapper(VoidCallable callable) {
			this.callable = callable;
		}
	
		@Override
		public Void call() throws Exception {
			callable.call();
			return null;
		}
	}

	public void call() throws Exception;		
}