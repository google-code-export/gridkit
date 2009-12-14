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
public class BenchmarkCommand implements Command<BenchmarkContext>,
		PortableObject, ExternalizableLite {


	private static final long serialVersionUID = 6101217481255920806L;
	
	private static final int COMMAND_TIME_INDEX = 0;
	
	private BenchmarkCommandTime commandTime;
	private static long counter;

	/**
	 * Default constructor. Initialize pushTime of currentTime in millisecond
	 */
	public BenchmarkCommand() {
		long pushTime = System.nanoTime();
		long id = (long) (Math.random() * 10000000) + (++counter);
		commandTime = new BenchmarkCommandTime();
		commandTime.setCommandId(id);
		commandTime.setPushTime(pushTime);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(
			ExecutionEnvironment<BenchmarkContext> executionEnvironment) {
		BenchmarkContext context = executionEnvironment.getContext();

		// Invoke execution method.
		execute();
		// Save time information
		commandTime.setEndTime(System.nanoTime());
		context.addCommandTime(commandTime);
		
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
		commandTime = (BenchmarkCommandTime) reader.readObject(COMMAND_TIME_INDEX);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(COMMAND_TIME_INDEX, commandTime);
	}


	@Override
	public void readExternal(DataInput in) throws IOException {
		commandTime = (BenchmarkCommandTime) ExternalizableHelper.readObject(in);

	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, commandTime);
	}

}
