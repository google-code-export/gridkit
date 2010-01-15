package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

import com.tangosol.net.cache.KeyAssociation;

public class PartitionKey extends AbstractMobileObject implements KeyAssociation {

	private Object id;
	
	public PartitionKey() {
		// for POF
	}
	
	public PartitionKey(Object id) {
		this.id = id;
	}

	@Override
	public Object getAssociatedKey() {
		return id;
	}

	@Override
	protected void readWireFormat(WireFormatIn in) throws IOException {
		id = in.readObject();		
	}

	@Override
	protected void writeWireFormat(WireFormatOut out) throws IOException {
		out.writeObject(id);
	}
}
