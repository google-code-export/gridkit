package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

public class CommandRef extends CommandRefKey implements Comparable<CommandRef> {

	private static final long serialVersionUID = 20100109L;

	/**
	 * Timestamp when command was scheduled (using safe cluster time)
	 */
	private long timestamp;
	private int subcounter; // required for resolving sub ms scheduling order
	
	private boolean started = false;
	
	public CommandRef() {
		// for POF
	}
	
	public CommandRef(Object contextKey, long msgUid, long timestamp, int subcounter) {
		super(contextKey, msgUid);
		this.timestamp = timestamp;
		this.subcounter = subcounter;
	}

	@Override
	public int compareTo(CommandRef o) {
		int n = Long.signum(timestamp - o.timestamp);
		return n == 0 ? subcounter - o.subcounter : n;
	}

	@Override
	protected void readWireFormat(WireFormatIn in) throws IOException {
		super.readWireFormat(in);
		timestamp = in.readLong();
		subcounter = in.readInt();
	}

	@Override
	protected void writeWireFormat(WireFormatOut out) throws IOException {
		super.writeWireFormat(out);
		out.writeLong(timestamp);
		out.writeInt(subcounter);
	}
}
