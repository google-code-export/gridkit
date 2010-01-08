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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class CascadeCommand implements Command<SimpleTestContext>, PortableObject {

	private static final long serialVersionUID = 20100105L;
	
	private long execId;
	private String reportBuffer;
	private long submitMs;
	private long submitNs;
	private Identifier[] contexts;
	private String taskHeader = "some random text to increase task size";
	private Map<?, ?> taskPayload = Collections.EMPTY_MAP;

	public CascadeCommand() {
		// for POF
	}

	public CascadeCommand(long execId, String reportBuffer, Map<?, ?> payload) {
		this(execId, reportBuffer);
		this.taskPayload = payload;
	}
	
	public CascadeCommand(long execId, String reportBuffer) {
		this.execId = execId;
		this.reportBuffer = reportBuffer;
		this.submitMs = System.currentTimeMillis();
		this.submitNs = System.nanoTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(ExecutionEnvironment<SimpleTestContext> executionEnvironment) {		
		// Invoke execution method.
		SimpleTestContext ctx = executionEnvironment.getContext();
		ctx.touch();
		executionEnvironment.setContext(ctx);
		// Save time information
		BenchmarkSupport.reportExecution(reportBuffer, new ExecMark(execId, submitMs, submitNs));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader in) throws IOException {
		int propId = 0;
		execId = in.readLong(propId++);
		submitMs = in.readLong(propId++);
		submitNs = in.readLong(propId++);
		reportBuffer = in.readString(propId++);
		contexts = (Identifier[]) in.readObjectArray(propId++, new Identifier[0]);
		taskHeader = in.readString(propId++);
		taskPayload = in.readMap(propId++, new HashMap());
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		int propId = 0;
		out.writeLong(propId++, execId);
		out.writeLong(propId++, submitMs);
		out.writeLong(propId++, submitNs);
		out.writeString(propId++, reportBuffer);
		out.writeObjectArray(propId++, contexts);
		out.writeString(propId++, taskHeader);
		out.writeMap(propId++, taskPayload);
	}
}
