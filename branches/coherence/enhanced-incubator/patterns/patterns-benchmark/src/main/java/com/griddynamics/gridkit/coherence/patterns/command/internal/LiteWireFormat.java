package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.util.ExternalizableHelper;

class LiteWireFormat implements WireFormatIn, WireFormatOut {

	int propId = 0;
	DataInput in;
	DataOutput out;
	
	public LiteWireFormat(DataInput reader) {
		in = reader;
	}
	
	public LiteWireFormat(DataOutput writer) {
		out = writer;
	}

	@Override
	public int readInt() throws IOException {
		return in.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return in.readLong();
	}

	@Override
	public Object readObject() throws IOException {
		return ExternalizableHelper.readObject(in);
	}

	@Override
	public void writeInt(int val) throws IOException {
		out.writeInt(val);
	}

	@Override
	public void writeLong(long val) throws IOException {
		out.writeLong(val);
	}

	@Override
	public void writeObject(Object val) throws IOException {
		ExternalizableHelper.writeObject(out, val);		
	}
}
