package org.gridkit.coherence.integration.spring.impl;

import java.util.concurrent.ThreadFactory;

class SimpleThreadFactory implements ThreadFactory {

	private final String name;
	private final ThreadGroup group;
	private final boolean daemon;
	
	public SimpleThreadFactory(ThreadGroup group, String name, boolean daemon) {
		this.group = group;
		this.name = name;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread;
		if (group != null) {
			thread = new Thread(group, r);
		}
		else {
			thread = new Thread(r);
		}

		if (name != null) {
			thread.setName(name);
		}
		
		thread.setDaemon(daemon);
					
		return thread;
	}
}
