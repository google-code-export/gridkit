package org.gridkit.coherence.misc.bulletproof;

import java.io.IOException;

import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.WriteBuffer.BufferOutput;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofSerializer;

/**
 * Fake POF context required for canary key filtering.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class DummyPofContext implements PofContext {

	@Override
	public void serialize(BufferOutput bo, Object obj) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object deserialize(BufferInput bo) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public PofSerializer getPofSerializer(int typeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUserTypeIdentifier(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int getUserTypeIdentifier(Class type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUserTypeIdentifier(String type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getClassName(int typeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class getClass(int typeId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserType(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isUserType(Class type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserType(String name) {
		throw new UnsupportedOperationException();
	}
}
