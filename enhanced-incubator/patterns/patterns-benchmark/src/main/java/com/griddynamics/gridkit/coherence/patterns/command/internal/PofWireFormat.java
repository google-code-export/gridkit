package com.griddynamics.gridkit.coherence.patterns.command.internal;

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
}
