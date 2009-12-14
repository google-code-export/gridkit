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

import java.io.Serializable;

/**
 * Entity for saving command benchmark data.
 * 
 * @author akornev
 * @since 1.0
 */
public class BenchmarkCommandTime implements Serializable {

	private static final long serialVersionUID = -5289310088868581802L;

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

}
