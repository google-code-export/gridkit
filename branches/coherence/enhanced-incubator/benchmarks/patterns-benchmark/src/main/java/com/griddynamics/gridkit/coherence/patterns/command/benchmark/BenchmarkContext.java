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
import java.util.HashMap;
import java.util.Map;

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
public class BenchmarkContext implements Context, PortableObject, ExternalizableLite {
	
	private static final int START_TIME_INDEX = 0;
	private static final int END_TIME_INDEX = 1;

	private static final long serialVersionUID = 512313983665949196L;
	private Map<Long, Long> startTimes;
	private Map<Long, Long> endTimes;

	/**
	 * Default constructor initialize start and end times
	 */
	public BenchmarkContext() {
		startTimes = new HashMap<Long, Long>();
		endTimes = new HashMap<Long, Long>();
	}
	
	/**
	 * Add start time for command with commandId
	 * 
	 * @param commandId - unique command identifier
	 * @param time - command start time
	 */
	public void addStartTimes(long commandId, long time) {
		startTimes.put(commandId, time);
	}
		
	/**
	 * Add end time for command with commandId.
	 * 
	 * @param commandId - unique command identifier
	 * @param time - command end time
	 */
	public void addEndTimes(long commandId,long time) {
		endTimes.put(commandId, time);
	}
	
	/**
	 * Get commands start times.
	 * 
	 * @return startTimes
	 */
	public Map<Long, Long> getStartTimes() {
		return startTimes;
	}
	
	/**
	 * Get commands end times.
	 * 
	 * @return endTimes
	 */
	public Map<Long, Long> getEndTimes() {
		return endTimes;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(PofReader reader) throws IOException {
		startTimes = (Map<Long, Long>) reader.readObject(START_TIME_INDEX);
		endTimes = (Map<Long, Long>) reader.readObject(END_TIME_INDEX);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(START_TIME_INDEX, startTimes);
		writer.writeObject(END_TIME_INDEX, endTimes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(DataInput in) throws IOException {
		startTimes = (Map<Long, Long>) ExternalizableHelper.readObject(in);
		endTimes = (Map<Long, Long>) ExternalizableHelper.readObject(in);
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, startTimes);
		ExternalizableHelper.writeObject(out, endTimes);
	}
	
}
