package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

public class CommandBodyKey extends ContextedKey {

	private static final long serialVersionUID = 20100109L;

	long msgUid;
	
	public CommandBodyKey() {
		// for POF
	}
	
	public CommandBodyKey(Object contextKey, long msgUid) {
		this.contextKey = contextKey;
		this.msgUid = msgUid;
	}
	
	@Override
	public Object getAssociatedKey() {		
		return contextKey == null ? Long.valueOf(msgUid) : super.getAssociatedKey();
	}

	@Override
	protected void readWireFormat(WireFormatIn in) throws IOException {
		super.readWireFormat(in);
		msgUid = in.readLong();		
	}

	@Override
	protected void writeWireFormat(WireFormatOut out) throws IOException {
		super.writeWireFormat(out);
		out.writeLong(msgUid);		
	}
}
