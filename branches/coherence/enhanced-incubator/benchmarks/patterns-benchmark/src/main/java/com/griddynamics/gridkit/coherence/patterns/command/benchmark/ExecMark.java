package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ExecMark implements Serializable, PortableObject {

	private static final long serialVersionUID = 20100105L;
	
	long execId;
	long submitMs;
	long submitNs;
	long execMs;
	long execNs;
	
	public ExecMark() {
		// for POF
	}
	
	public ExecMark(long execId, long submitMs, long submitNs) {
		this.execId = execId;
		this.submitMs = submitMs;
		this.submitNs = submitNs;
		this.execMs = System.currentTimeMillis();
		this.execNs = System.nanoTime();
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		int propId = 0;
		this.execId = in.readLong(propId++);
		this.submitMs = in.readLong(propId++);
		this.submitNs = in.readLong(propId++);
		this.execMs = in.readLong(propId++);
		this.execNs = in.readLong(propId++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int propId = 0;
		out.writeLong(propId++, execId);
		out.writeLong(propId++, submitMs);
		out.writeLong(propId++, submitNs);
		out.writeLong(propId++, execMs);
		out.writeLong(propId++, execNs);
	}	
}
