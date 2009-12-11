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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * Command for testing command pattern
 * 
 * @author akornev@griddynamics.com
 * @since 1.0
 */
public class BenchmarkCommand implements Command<BenchmarkContext>, PortableObject,
		ExternalizableLite {

	private static final int PUSH_TIME_SERIALIZE_INDEX = 0;

	private static final long serialVersionUID = 6101217481255920806L;
	private long pushTime;
	private long id;
	private static long counter;

	/**
	 * Default constructor. Initialize pushTime of currentTime in millisecond
	 */
	public BenchmarkCommand() {
		pushTime = System.currentTimeMillis();
		id = (long) (Math.random() * 10000000) + (++counter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(ExecutionEnvironment<BenchmarkContext> executionEnvironment) {
		BenchmarkContext context = executionEnvironment.getContext();
		context.addStartTimes(id, pushTime);
		// Invoke execution method.
		execute();

		context.addEndTimes(id, System.currentTimeMillis());
		executionEnvironment.setContext(context);

	}

	private void execute() {
		// TODO: insert some code
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readExternal(PofReader reader) throws IOException {
		pushTime = reader.readLong(PUSH_TIME_SERIALIZE_INDEX);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(PUSH_TIME_SERIALIZE_INDEX, pushTime);
	}

	/**
	 * Get time when command pushed.
	 * 
	 * @return time
	 */
	public long getPushTime() {
		return pushTime;
	}

	/**
	 * Set time when command pushed.
	 * 
	 * @param pushTime
	 */
	public void setPushTime(long pushTime) {
		this.pushTime = pushTime;
	}

	@Override
	public void readExternal(DataInput in) throws IOException {
		pushTime = ExternalizableHelper.readLong(in);

	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeLong(out, pushTime);

	}

}
