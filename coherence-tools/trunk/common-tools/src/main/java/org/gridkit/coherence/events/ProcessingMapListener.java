package org.gridkit.coherence.events;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;

public class ProcessingMapListener implements MapListener {

	private BackingMapManagerContext context;
	private ExecutorService threadPool;
	
	public ProcessingMapListener(String cacheName, BackingMapManagerContext context) {
		this(cacheName, context, 10);
	}

	public ProcessingMapListener(final String cacheName, BackingMapManagerContext context, int threadCount) {
		System.out.println("ProcessingMapListener added to a cache [" + cacheName + "]");
		this.context = context;
		threadPool = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
			
			private AtomicInteger counter = new AtomicInteger();
			
			@Override
			public Thread newThread(Runnable r) {
				Thread th = new Thread(r);
				th.setName("TaskProcessor:" + cacheName + ":" + counter);
				return th;
			}
		});
	}
	
	@Override
	public void entryDeleted(MapEvent event) {
		System.err.println("EntryDeleted: " + event);		
		// not interested
	}

	@Override
	public void entryInserted(MapEvent event) {
		System.err.println("EntryInserted: " + event);
		Object tt = context.getValueFromInternalConverter().convert(event.getNewValue());
		// processing only Runnable, ignoring everything else
		if (tt instanceof Runnable) {
			System.err.println("Task submited: " + event.getKey() + " - " + tt);
			threadPool.submit(new TaskProcessor(event, (Runnable)tt));
		}		
	}

	@Override
	public void entryUpdated(MapEvent event) {
		System.err.println("EntryUpdated: " + event);
		// not interested
	}
	
	private static class TaskProcessor implements Runnable {

		private Map<?, ?> backingMap;
		private Object key;
		private Runnable ticket;
		
		public TaskProcessor(MapEvent event, Runnable ticket) {
			this.backingMap = event.getMap();
			this.key = event.getKey();
			this.ticket = ticket;
		}

		@Override
		public void run() {
			if (backingMap.containsKey(key)) {
				try {
					ticket.run();
				}
				catch(Throwable e) {
					CacheFactory.log("Execption in async task: " + e.toString(), Base.LOG_WARN);
				}
				finally {
					System.err.println("Task completed: " + key);
					backingMap.remove(key);
				}
			}
		}
	}
}
