package org.gridkit.lab.zktest;

import java.io.Serializable;
import java.rmi.Remote;

public abstract class AbstractAllReducer<T, V> implements Reducer<T, V>, DataSink<T>, DataSource<V>, Serializable {

	private static final long serialVersionUID = 20131116L;

	private RootReducer<T, V> root;
	
	public AbstractAllReducer() {
		root = new Root<T, V>(this);
	}

	@Override
	public void add(T object) {
		root.push(System.getProperty("vinode.name"), object);
	}

	@Override
	public V reduce() {
		return root.pull();
	}

	
	@Override
	public V pull() {
		return reduce();
	}

	@Override
	public void push(T value) {
		add(value);
	}

	protected abstract void collect(String nodename, T value);

	protected abstract V calculate();
	
	
	private static interface RootReducer<T, V> extends Remote {
		
		public void push(String nodename, T value);
		
		public V pull();
		
	}
	
	private static class Root<T, V> implements RootReducer<T, V> {
		
		final AbstractAllReducer<T, V> reducer;
		boolean reduced;
		V reduction;

		public Root(AbstractAllReducer<T, V> reducer) {
			this.reducer = reducer;
		}

		@Override
		public synchronized void push(String nodename, T value) {
			if (reduced) {
				throw new IllegalStateException("Already reduced");
			}
			reducer.collect(nodename, value);
		}

		@Override
		public synchronized V pull() {
			if (!reduced) {
				reduction = reducer.calculate(); 
				reduced = true;
			}
			return reduction;
		}
	}
}
