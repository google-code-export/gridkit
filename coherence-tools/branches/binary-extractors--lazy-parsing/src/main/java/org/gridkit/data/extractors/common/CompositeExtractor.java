package org.gridkit.data.extractors.common;

import java.util.List;

import org.gridkit.data.extractors.common.RetrivalControl.InputStatus;

public interface CompositeExtractor<V> extends BinaryExtractor<V> {

	public List<BinaryExtractor<?>> getSubExtractors();
	
	public VectorResultReceiver newComposer(CompositionCallback callback);
	
	interface CompositionCallback extends ScalarResultReceiver, RetrivalControl {
		
	}
	
	public static abstract class SingleArgumentComposer implements VectorResultReceiver {

		protected CompositionCallback callback;
		
		public SingleArgumentComposer(CompositionCallback callback) {
			this.callback = callback;
			this.callback.setInputStatus(0, InputStatus.ACCEPT);
		}

		@Override
		public void push(int id, Object part) {
			if (id == 0) {
				processInput(part, callback);
			}
			else {
				throw new IllegalArgumentException("No such input slot #" + id);
			}
		}

		@Override
		public void done() {
		}

		protected abstract void processInput(Object input, ScalarResultReceiver receiver);
	}
}
