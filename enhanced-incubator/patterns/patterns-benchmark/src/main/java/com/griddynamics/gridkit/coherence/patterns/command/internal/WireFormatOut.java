package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

public interface WireFormatOut {

	void writeObject(Object obj) throws IOException;

	void writeLong(long msgId);

	void writeInt(int subcounter);

}
