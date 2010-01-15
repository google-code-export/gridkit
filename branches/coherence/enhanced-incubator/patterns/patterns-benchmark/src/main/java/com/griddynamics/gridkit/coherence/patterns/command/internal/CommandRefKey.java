package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

public class CommandRefKey extends ContextedKey {

	private static final long serialVersionUID = 20100110L;
	
	long msgUid;
	
	public CommandRefKey() {
		// for POF serialization
	}
	
	public CommandRefKey(Object contextKey, long msgUid) {
		this.contextKey = contextKey;
		this.msgUid = msgUid;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (msgUid ^ (msgUid >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandRefKey other = (CommandRefKey) obj;
		if (msgUid != other.msgUid)
			return false;
		return true;
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
