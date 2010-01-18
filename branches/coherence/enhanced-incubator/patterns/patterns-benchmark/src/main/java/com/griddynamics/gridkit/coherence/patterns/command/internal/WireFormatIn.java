package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;

public interface WireFormatIn {

	Object readObject() throws IOException;

	long readLong() throws IOException;

	int readInt() throws IOException;

}
