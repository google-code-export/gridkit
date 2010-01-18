package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class CommandQueues {
	
	private static ConcurrentMap<Object, CommandQueue> queues = new ConcurrentHashMap<Object, CommandQueue>();
	
	public static void enqueue(CommandRef ref) {
		while(true) {
			Object context = ref.contextKey;
			CommandQueue queue = queues.get(context);
			if (queue == null) {
				// TODO is it efficient?
				queue = new TempBuffer();
				queues.putIfAbsent(context, queue);
				queue = queues.get(context);
			}		
			try {
				queue.push(ref);
			}
			catch(IllegalStateException e) {
				continue;
			}
			break;
		}
	}
	
	public static Collection<CommandRef> replaceQueue(Object contextKey, CommandQueue queue) {
		CommandQueue cmdQ = queues.get(contextKey);
		Collection<CommandRef> oldRefs = AnyType.cast(Collections.EMPTY_LIST);
		if (cmdQ instanceof TempBuffer) {
			TempBuffer buf = (TempBuffer) cmdQ;
			oldRefs = buf.disable();
		}
		queues.replace(contextKey, cmdQ, queue);
		return oldRefs;
	}
	
	public static interface CommandQueue {
		public void push(CommandRef ref); 
	}
	
	private static class TempBuffer implements CommandQueue {
		List<CommandRef> buffer = new ArrayList<CommandRef>();

		@Override
		public synchronized void push(CommandRef ref) {
			if (buffer == null) {
				throw new IllegalStateException();
			}
			else {
				buffer.add(ref);
			}
		}

		public synchronized Collection<CommandRef> disable() {
			List<CommandRef> buffer = this.buffer;
			this.buffer = null;
			return buffer;
		}
	}
}
