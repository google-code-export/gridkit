/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;

import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * Context for test command performance
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SimpleTestContext implements Context, PortableObject {

	private static final long serialVersionUID = 20100105L;
	
	private String contextName;
	private long counter;
	
	public SimpleTestContext() {
		// for POF
	}
	
	public SimpleTestContext(String contextName) {
		this.contextName = contextName;
	}
	
	public void touch() {
		++counter;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		int propId = 0;
		this.contextName = in.readString(propId++);
		this.counter = in.readLong(propId++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int propId = 0;
		out.writeString(propId++, contextName);
		out.writeLong(propId++, counter);
	}
}
