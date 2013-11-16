package org.gridkit.lab.zktest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Reducers {

	public static <T> AbstractAllReducer<T, Collection<T>> collect() {
		return new CollectReducer<T>(); 
	}

	public static <T> AbstractAllReducer<T, T> first() {
		return new FirstReducer<T>(); 
	}
	
	private static class CollectReducer<T> extends AbstractAllReducer<T, Collection<T>> {

		private static final long serialVersionUID = 20131116L;

		private transient List<T> collection = new ArrayList<T>();
		
		@Override
		protected void collect(String nodename, T value) {
			collection.add(value);
		}

		@Override
		protected Collection<T> calculate() {
			return collection;
		}
	}

	private static class FirstReducer<T> extends AbstractAllReducer<T, T> {

		private static final long serialVersionUID = 20131116L;

		private transient boolean set = false;
		private transient T result;
		
		@Override
		protected void collect(String nodename, T value) {
			set = true;
			result = value;
		}

		@Override
		protected T calculate() {
			return result;
		}
	}
}
