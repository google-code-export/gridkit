package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.net.cache.KeyAssociation;

abstract class ContextedKey extends AbstractMobileObject implements KeyAssociation {

	private static final long serialVersionUID = 20100109L;
	
	Object contextKey;
	
	@Override
	public Object getAssociatedKey() {
		return contextKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contextKey == null) ? 0 : contextKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContextedKey other = (ContextedKey) obj;
		if (contextKey == null) {
			if (other.contextKey != null)
				return false;
		} else if (!contextKey.equals(other.contextKey))
			return false;
		return true;
	}

	protected void readWireFormat(WireFormatIn in) throws IOException {
		contextKey = in.readObject();
	}

	protected void writeWireFormat(WireFormatOut out) throws IOException {
		out.writeObject(contextKey);
	}	
	
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
