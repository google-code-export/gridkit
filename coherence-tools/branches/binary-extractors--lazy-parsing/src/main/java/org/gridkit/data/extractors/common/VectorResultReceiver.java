package org.gridkit.data.extractors.common;

public interface VectorResultReceiver {

	public void push(int id, Object part);
	
	public void done();
	
	public static class VectorResult implements VectorResultReceiver {
		
		private final Object[] vector;
		
		public VectorResult(int size) {
			this.vector = new Object[size];
		}

		@Override
		public void push(int id, Object part) {
			vector[id] = part;
		}
		
		@Override
		public void done() {
			// do nothing
		}

		public Object[] getVector() {
			return vector;
		}
	}
}
