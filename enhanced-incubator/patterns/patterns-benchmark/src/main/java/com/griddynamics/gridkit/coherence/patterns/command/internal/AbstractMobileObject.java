package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * Helper base class to uniformly handle POF and eternalizeable lite serialization.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
abstract class AbstractMobileObject implements PortableObject, ExternalizableLite, Serializable {

	private static final long serialVersionUID = 20100109L;
	
//	@Override
//	public abstract int hashCode();
//
//	@Override
//	public abstract boolean equals(Object obj);

	protected abstract void readWireFormat(WireFormatIn in) throws IOException;

	protected abstract void writeWireFormat(WireFormatOut out) throws IOException;
	
	@Override
	public void readExternal(PofReader in) throws IOException {
		readWireFormat(new PofWireFormat(in));
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		writeWireFormat(new PofWireFormat(out));		
	}

	@Override
	public void readExternal(DataInput in) throws IOException {
		readWireFormat(new LiteWireFormat(in));
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		writeWireFormat(new LiteWireFormat(out));		
	}
}
