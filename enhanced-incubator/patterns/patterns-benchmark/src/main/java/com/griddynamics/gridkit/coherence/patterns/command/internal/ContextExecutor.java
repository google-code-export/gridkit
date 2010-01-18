package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.griddynamics.gridkit.coherence.patterns.command.ContextConfigurationScheme;
import com.griddynamics.gridkit.coherence.patterns.command.internal.OwnershipDaemon.OwnershipListener;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.util.ClassFilter;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.OrFilter;
import com.tangosol.util.filter.PartitionedFilter;

class ContextExecutor implements OwnershipListener {
	
	private final String schemeName;
	private final boolean collocatedCommands;
	private final NamedCache contextCache;
	private final NamedCache commandCache;
	private final DistributedCacheService cacheService;

	private final PartitionSet partitionSet;
	private final ReadWriteLock partitionUpdateLock;
	
	private final ConcurrentMap<Object, CommandRef[]> activeContexts = new ConcurrentHashMap<Object, CommandRef[]>();
	private final CommandRef[] busyContextMark = new CommandRef[0];
	
	private final QueueFacade queueFacade = new QueueFacade();
	private final Queue<CommandRef> inqueue = new PriorityBlockingQueue<CommandRef>();
	
	private final ExecutorService executor;
	private final TaskPoller taskPoller = new TaskPoller();
	
	
	public ContextExecutor(ContextConfigurationScheme scheme) {
		this.schemeName = scheme.getSchemeName();
		this.contextCache = CacheFactory.getCache(scheme.getContextCacheName());
		this.commandCache = CacheFactory.getCache(scheme.getCommandCacheName());
		this.cacheService = (DistributedCacheService) contextCache.getCacheService();
		this.collocatedCommands = scheme.areCommandsCollocated();
		
		this.partitionSet = new PartitionSet(this.cacheService.getPartitionCount());
		
		this.partitionUpdateLock = new ReentrantReadWriteLock();
		this.executor = Executors.newFixedThreadPool(5); // TODO temporary, make configurable
	}
	
	private void recover(PartitionSet set) {
		Filter contextF = new ClassFilter(ContextMetaData.class);
		Filter commandF = new ClassFilter(CommandRef.class);
		Filter finalF = new PartitionedFilter(new OrFilter(contextF, commandF), set);
		
		Set<Object> contexts = new HashSet<Object>();		
		Set<Map.Entry<?, ?>> entries = AnyType.cast(contextCache.entrySet(finalF));
		int tasksPending = 0; 

		for(Map.Entry<?, ?> entry: entries) {
			Object val = entry.getValue();
			if (val instanceof ContextMetaData) {
				ContextMetaData ctx = (ContextMetaData) val;
				if (schemeName.equals(ctx.schemeName)) {
					contexts.add(ctx.contextKey);
					Collection<CommandRef> queue = CommandQueues.replaceQueue(ctx.contextKey, queueFacade);
					tasksPending += queue.size();
					inqueue.addAll(queue);
				}
			}
		}

		for(Map.Entry<?, ?> entry: entries) {
			Object val = entry.getValue();
			if (val instanceof CommandRef) {
				CommandRef ref = (CommandRef) val;
				if (contexts.contains(ref.contextKey)) {
					inqueue.add(ref);
					++tasksPending;
				}
			}
		}
		
		if (tasksPending > 5) {
			tasksPending = 5; // TODO use configured thread pool size
		}
		
		for(; tasksPending > 0; --tasksPending) {
			executor.submit(taskPoller);
		}
	}
	
	private class TaskPoller implements Runnable {		
		@Override
		public void run() {
			CommandRef ref = inqueue.poll();
			if (ref != null) {
				Object contextKey = ref.contextKey;
				while(true) {
					CommandRef[] queued = activeContexts.get(contextKey);
					if (queued == null) {
						if (activeContexts.putIfAbsent(contextKey, busyContextMark) == null) {
							execute(ref);
							return;
						}
						else {
							break;
						}
					}
					else {
						CommandRef[] newq = Arrays.copyOf(queued, queued.length + 1);
						newq[queued.length] = ref;
						if (activeContexts.replace(contextKey, queued, newq)) {
							return;
						}
					}
				}
			}
		}

		private void execute(CommandRef ref) {
			partitionUpdateLock.readLock().lock();
			executeCommand(ref);
			partitionUpdateLock.readLock().unlock();
			
			if (activeContexts.remove(ref, busyContextMark)) {
				if (inqueue.size() > 0) {
					executor.submit(this);
				}
			}
			else {
				executor.submit(new QueuedTaskPoller(ref.contextKey));
			}
		}

	}
	
	private class QueuedTaskPoller implements Runnable {
		
		private Object contextKey;
		
		public QueuedTaskPoller(Object contextKey) {
			this.contextKey = contextKey;
		}

		@Override
		public void run() {
			CommandRef[] buf = activeContexts.get(contextKey);
			for(CommandRef ref: buf) {
				executeCommand(ref);
			}
			if (activeContexts.remove(contextKey, buf)) {
				if (inqueue.size() > 0) {
					executor.submit(this);
				}
			}
			else {
				while(true) {
					CommandRef[] buf2 = activeContexts.get(contextKey);
					CommandRef[] remainder = new CommandRef[buf2.length - buf.length];
					assert remainder.length > 0;
					System.arraycopy(buf2, buf.length, remainder, 0, remainder.length);
					if (activeContexts.replace(contextKey, buf2, remainder)) {
						break;
					}
				}
				executor.submit(this);
			}
		}
	}
	
	private void executeCommand(CommandRef ref) {
		if (partitionSet.contains(cacheService.getKeyPartitioningStrategy().getKeyPartition(ref.contextKey))) {
			CommandBodyKey bodyKey = new CommandBodyKey(collocatedCommands ? ref.contextKey : null, ref.msgUid);
			Object command = commandCache.get(bodyKey); 
			if (command != null) {
				Command<Context> cmd = AnyType.cast(command);
				ContextEnvironment<Context> env = new ContextEnvironment<Context>(ref, contextCache);
				
				try {
					cmd.execute(env);
				}
				catch(Exception e) {
					e.printStackTrace();
					// TODO exception handling: command execution
				}
				
				// TODO async cache operations
				commandCache.remove(bodyKey);
				contextCache.remove(new CommandRefKey(ref));
			}
			else {
				// TODO logging: command is not found
			}
		}
		else {
			// TODO logging: unowned object
			// ownership has been moved to another member
		}
	}
	
	private static class ContextEnvironment<C extends Context> implements ExecutionEnvironment<C> {

		private CommandRef cmdRef;
		private NamedCache contextCache;

		public ContextEnvironment(CommandRef cmdRef, NamedCache contextCache) {
			this.cmdRef = cmdRef;
			this.contextCache = contextCache;
		}

		@Override
		public C getContext() {
			return AnyType.<C>cast(contextCache.get(cmdRef.contextKey));
		}

		@Override
		public ContextConfiguration getContextConfiguration() {
			// TODO incomplete ExecutionEnvironment implementation
			throw new UnsupportedOperationException();
		}

		@Override
		public Identifier getContextIdentifier() {
			return (Identifier) cmdRef.contextKey;
		}

		@Override
		public Ticket getTicket() {
			// TODO incomplete ExecutionEnvironment implementation
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasCheckpoint() {
			// TODO incomplete ExecutionEnvironment implementation
			return false;
		}

		@Override
		public boolean isRecovering() {
			// TODO incomplete ExecutionEnvironment implementation
			return false;
		}

		@Override
		public Object loadCheckpoint() {
			// TODO incomplete ExecutionEnvironment implementation
			throw new UnsupportedOperationException();
		}

		@Override
		public void removeCheckpoint() {
			// TODO incomplete ExecutionEnvironment implementation
			throw new UnsupportedOperationException();			
		}

		@Override
		public void saveCheckpoint(Object state) {
			// TODO incomplete ExecutionEnvironment implementation
			throw new UnsupportedOperationException();			
		}

		@Override
		public void setContext(C context) {
			contextCache.put(cmdRef.contextKey, context);
		}
	}
	
	private class QueueFacade implements CommandQueues.CommandQueue {
		@Override
		public void push(CommandRef ref) {
			inqueue.add(ref);
			executor.execute(taskPoller);
		}
	}	
	
	@Override
	public void partitionsOwnershipChanged(DistributedCacheService service,	String cacheName, PartitionSet withdrawn, PartitionSet assigned) {
		if ((withdrawn != null && !withdrawn.isEmpty()) || (assigned != null && !assigned.isEmpty())) {
			partitionUpdateLock.writeLock().lock();
			try {
				// no context execution is permitted during partitionSet update
				if (withdrawn != null) {
					partitionSet.remove(withdrawn);				
				}
				partitionSet.add(assigned);
				recover(assigned);
			}
			finally {
				partitionUpdateLock.writeLock().unlock();
			}
		}
	}		
}
