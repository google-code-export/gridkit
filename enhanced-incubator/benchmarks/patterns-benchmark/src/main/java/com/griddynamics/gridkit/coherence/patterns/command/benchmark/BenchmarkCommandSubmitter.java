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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.CommandSubmitter;

/**
 * Command submitter. Submit command count time.
 * 
 * @author akornev@griddynamics.com
 * @since 1.0
 */
public class BenchmarkCommandSubmitter implements Runnable {

	private CommandSubmitter commandSubmitter;
	private Identifier identifier;
	private int count;

	/**
	 * Default constructor
	 */
	public BenchmarkCommandSubmitter() {
	}

	/**
	 * Constructor initialize all fields.
	 * 
	 * @param commandSubmitter
	 * @param identifier
	 * @param count
	 * @param command
	 */
	public BenchmarkCommandSubmitter(CommandSubmitter commandSubmitter,
			Identifier identifier, int count) {
		this.commandSubmitter = commandSubmitter;
		this.identifier = identifier;
		this.count = count;
	}

	/**
	 * Get submit count.
	 * 
	 * @return submit count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Set submit count
	 * 
	 * @param count
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Get {@link Identifier}
	 * 
	 * @return {@link Identifier}
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * Set {@link Identifier}
	 * 
	 * @param identifier
	 *            - {@link Identifier}
	 */
	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * Get command submitter.
	 * 
	 * @return command subbmiter
	 */
	public CommandSubmitter getCommandSubmitter() {
		return commandSubmitter;
	}

	/**
	 * Set command subbmitter
	 * 
	 * @param commandSubmitter
	 */
	public void setCommandSubmitter(CommandSubmitter commandSubmitter) {
		this.commandSubmitter = commandSubmitter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		for (int i = 0; i < count; i++) {
			commandSubmitter.submitCommand(identifier, new BenchmarkCommand());
		}
	}
}
