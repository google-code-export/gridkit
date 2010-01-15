package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

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
}
