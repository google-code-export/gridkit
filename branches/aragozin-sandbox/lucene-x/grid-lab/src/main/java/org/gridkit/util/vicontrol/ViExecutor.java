package org.gridkit.util.vicontrol;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface ViExecutor {

	public void exec(Runnable task);
	
	public void exec(VoidCallable task);

	public <T> T exec(Callable<T> task);
	
	public Future<Void> submit(Runnable task);
	
	public Future<Void> submit(VoidCallable task);
	
	public <T> Future<? super T> submit(Callable<T> task);	

	// Mass operations

	/**
	 * Version of exec for group
	 * 
	 * @return
	 */
	public <T> List<? super T> massExec(Callable<T> task);
	
	public List<Future<Void>> massSubmit(Runnable task);
	
	public List<Future<Void>> massSubmit(VoidCallable task);
	
	public <T> List<Future<? super T>> massSubmit(Callable<T> task);
	
	public static interface VoidCallable {		
		public void call() throws Exception;		
	}	
}
