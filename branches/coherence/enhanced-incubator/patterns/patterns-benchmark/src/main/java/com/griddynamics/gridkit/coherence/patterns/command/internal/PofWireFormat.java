package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

class PofWireFormat implements WireFormatIn, WireFormatOut {

	int propId = 0;
	PofReader in;
	PofWriter out;
	
	public PofWireFormat(PofReader reader) {
		in = reader;
	}
	
	public PofWireFormat(PofWriter writer) {
		out = writer;
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt(propId++);
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong(propId++);
	}

	@Override
	public Object readObject() throws IOException {
		return in.readObject(propId++);
	}

	@Override
	public void writeInt(int val) throws IOException {
		out.writeInt(propId++, val);
	}

	@Override
	public void writeLong(long val) throws IOException {
		out.writeLong(propId++, val);		
	}

	@Override
	public void writeObject(Object val) throws IOException {
		out.writeObject(propId++, val);		
	}
}
