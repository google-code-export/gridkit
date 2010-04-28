package com.griddynamics.coherence.integration.spring;

import java.io.IOException;

import com.tangosol.io.Serializer;
import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.WriteBuffer.BufferOutput;

public class MockSerializer implements Serializer {
	public MockSerializer() {
		System.out.println("new MockSerializer");
	}
	
	public Object deserialize(BufferInput paramBufferInput) throws IOException {
		return null;
	}
	
	public void serialize(BufferOutput paramBufferOutput, Object paramObject)
			throws IOException {
	}
}
