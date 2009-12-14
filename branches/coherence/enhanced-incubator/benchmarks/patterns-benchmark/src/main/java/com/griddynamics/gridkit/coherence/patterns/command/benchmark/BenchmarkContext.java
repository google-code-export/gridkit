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
import java.util.ArrayList;
import java.util.List;

import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * Context for test command performance
 * 
 * @author akornev@griddynamics.com
 * @since 1.0
 */
public class BenchmarkContext implements Context, PortableObject,
		ExternalizableLite {

	private static final long serialVersionUID = 512313983665949196L;
	
	private static final int COMMAND_TIME_INDEX = 1;
	private List<BenchmarkCommandTime> commandTimes;

	/**
	 * Default constructor initialize start and end times
	 */
	public BenchmarkContext() {
		commandTimes = new ArrayList<BenchmarkCommandTime>();
	}
	
	/**
	 * Add command time.
	 * 
	 * @param commandTime - information about command time
	 */
	public void addCommandTime(BenchmarkCommandTime commandTime) {
		commandTimes.add(commandTime);
	}

	/**
	 * Get command times
	 * @return command times
	 */
	public List<BenchmarkCommandTime> getCommandTimes() {
		return commandTimes;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(PofReader reader) throws IOException {
		commandTimes = (List<BenchmarkCommandTime>) reader
				.readObject(COMMAND_TIME_INDEX);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(COMMAND_TIME_INDEX, commandTimes);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(DataInput in) throws IOException {
		commandTimes = (List<BenchmarkCommandTime>) ExternalizableHelper
				.readObject(in);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, commandTimes);
	}

}
