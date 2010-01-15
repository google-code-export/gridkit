package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

class CommandQueues {
	
	private static ConcurrentMap<Object, BlockingQueue<CommandRef>> queues = new ConcurrentHashMap<Object, BlockingQueue<CommandRef>>();
	
	public static void enqueue(CommandRef ref) {
		Object context = ref.contextKey;
		BlockingQueue<CommandRef> queue = queues.get(context);
		if (queue == null) {
			// TODO is it efficient?
			queue = new PriorityBlockingQueue<CommandRef>();
			queues.putIfAbsent(context, queue);
			queue = queues.get(context);
		}
		
		try {
			queue.put(ref);
		} catch (InterruptedException e) {
			// XXX should never happen
		}		
	}

}
