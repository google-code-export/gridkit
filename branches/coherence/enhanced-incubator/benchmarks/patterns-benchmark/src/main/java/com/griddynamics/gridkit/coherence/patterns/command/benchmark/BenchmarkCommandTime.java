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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * Entity for saving command benchmark data.
 * 
 * @author akornev
 * @since 1.0
 */
public class BenchmarkCommandTime implements ExternalizableLite, PortableObject {

	private static final long serialVersionUID = -5289310088868581802L;

	private static final int COMMAND_ID_INDEX = 0;

	private static final int PUSH_TIME_INDEX = 1;

	private static final int END_TIME_INDEX = 2;

	private long commandId;
	private long pushTime;
	private long endTime;

	/**
	 * Default constructor.
	 */
	public BenchmarkCommandTime() {
	}

	/**
	 * Constructor initialize all fields.
	 * 
	 * @param commandId - unique command id
	 * @param pushTime - time when command has pushed
	 * @param endTime - time when command has executed
	 */
	public BenchmarkCommandTime(long commandId, long pushTime, long endTime) {
		this.commandId = commandId;
		this.pushTime = pushTime;
		this.endTime = endTime;
	}

	/**
	 * Get unique command id.
	 * 
	 * @return unique command id
	 */
	public long getCommandId() {
		return commandId;
	}

	/**
	 * Set unique command id.
	 * 
	 * @param commandId
	 *            - unique command id
	 */
	public void setCommandId(long commandId) {
		this.commandId = commandId;
	}

	/**
	 * Get command push time.
	 * 
	 * @return command push time
	 */
	public long getPushTime() {
		return pushTime;
	}

	/**
	 * Set command push time.
	 * 
	 * @param pushTime
	 *            - time when command has pushed
	 */
	public void setPushTime(long pushTime) {
		this.pushTime = pushTime;
	}

	/**
	 * Get end command time.
	 * 
	 * @return end command time
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Set end command time.
	 * 
	 * @param endTime
	 *            - time when command has executed
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Override
	public void readExternal(DataInput in) throws IOException {
		commandId = in.readLong();
		pushTime = in.readLong();
		endTime = in.readLong();
		
	}

	@Override
	public void writeExternal(DataOutput out) throws IOException {
		out.writeLong(commandId);
		out.writeLong(pushTime);
		out.writeLong(endTime);
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		commandId = reader.readLong(COMMAND_ID_INDEX);
		pushTime = reader.readLong(PUSH_TIME_INDEX);
		endTime = reader.readLong(END_TIME_INDEX);		
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(COMMAND_ID_INDEX, commandId);
		writer.writeLong(PUSH_TIME_INDEX, pushTime);
		writer.writeLong(END_TIME_INDEX, endTime);	
	}

}
