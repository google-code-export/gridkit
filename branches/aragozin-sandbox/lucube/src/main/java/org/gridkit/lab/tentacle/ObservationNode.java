package org.gridkit.lab.tentacle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ObservationNode implements ObservationHost, StartStop {
	
	private final String id;
	private final SampleSink sink;
	private final Observer<Sample> observer;
	
	
	private int childCount;
	
	private boolean started;
	private boolean stopped;

	private List<StartStop> bound = new ArrayList<StartStop>();

	public ObservationNode(String id, SampleSink sink) {
		this.id = id;
		this.sink = sink;
		this.observer = new SimpleObserver<Sample>(id, sink);
	}
	
	public String getId() {
		return id;
	}

	public SampleSink getSink() {
		return sink;
	}
	
	public synchronized int newChildId() {
		return ++childCount;
	}
	
	@Override
	public synchronized ObservationHost createChildHost() {
		String suffix = TenUtils.generateId(newChildId());
		String cid = id + suffix;
		ObservationNode child = new ObservationNode(cid, sink.newChildSink(suffix));
		attach(child);
		return child;
	}

	public synchronized void attach(StartStop entity) {
		bound.add(entity);
		if (started && !stopped) {
			safeStart(entity);
		}
	}
	
	@Override
	public <S extends Sample> Observer<S> observer(Class<S> sample) {
		return observer(sample);
	}

	@Override
	public void addActivity(final ObservationActivity activity) {
		attach(new StartStop() {
			@Override
			public void start() {
				activity.start();
			}
			
			@Override
			public void stop() {
				activity.stop();
			}
			
			public String toString() {
				return activity.toString();
			}
		});
	}

	@Override
	public void destroy() {
		stop();
	}

	@Override
	public void reportError(String message, Throwable error) {
		sink.send(Metrics.alert(message, error));
	}

	@Override
	public synchronized void start() {
		if (started) {
			throw new IllegalArgumentException("Already started");
		}
		started = true;
		for(StartStop ss: bound) {
			safeStart(ss);
		}
	}

	private void safeStart(StartStop ss) throws ThreadDeath {
		try {
			ss.start();
		}
		catch(ThreadDeath e) {
			throw e;
		}
		catch(Throwable e) {
			String name = TenUtils.toSafeString(ss);
			sink.send(Metrics.alert("Execption on start() at " + name, e));
		}
	}


	@Override
	public synchronized void stop() {
		if (stopped) {
			// destroy and stop may not be coordinated, so alow multiple call to stop
			return;
		}
		if (!started) {
			throw new IllegalArgumentException("Not started");
		}
		started = true;
		for(StartStop ss: bound) {
			try {
				ss.stop();
			}
			catch(ThreadDeath e) {
				throw e;
			}
			catch(Throwable e) {
				String name = TenUtils.toSafeString(ss);
				sink.send(Metrics.alert("Execption on stop() at " + name, e));
			}
		}
		
		bound = Collections.emptyList();
	}
	
	private static class SimpleObserver<T extends Sample> implements Observer<T> {

		private final String id;
		private final SampleSink sink;
		
		public SimpleObserver(String id, SampleSink sink) {
			this.id = id;
			this.sink = sink;
		}

		@Override
		public void observe(T tuple) {
			sink.send(tuple);
		}

		@Override
		public void observe(T tuple, double timestamp) {
			sink.send(tuple, timestamp);
		}
	}
}
