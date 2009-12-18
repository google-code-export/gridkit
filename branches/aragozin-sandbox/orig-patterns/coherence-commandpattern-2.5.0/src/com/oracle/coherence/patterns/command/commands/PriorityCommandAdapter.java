/*
 * File: PriorityCommandAdapter.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.patterns.command.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.command.PriorityCommand;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link PriorityCommandAdapter} turn a regular {@link Command} into
 * a PriorityCommand.</p>
 *  
 * @author Brian Oliver
 *
 * @param <C>
 */
@SuppressWarnings("serial")
public class PriorityCommandAdapter<C extends Context> implements PriorityCommand<C>, ExternalizableLite, PortableObject {

	/**
	 * <p>The underlying {@link Command} to be made into a {@link PriorityCommand}.</p>
	 */
	private Command<C> command;
	
	
	/**
	 * <p>For {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public PriorityCommandAdapter() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param command
	 */
	public PriorityCommandAdapter(Command<C> command) {
		this.command = command;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<C> executionEnvironment) {
		command.execute(executionEnvironment);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(DataInput in) throws IOException {
		this.command = (Command<C>)ExternalizableHelper.readObject(in);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, command);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		this.command = (Command<C>)reader.readObject(0);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, command);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("PriorityCommandAdapter{%s}", command);
	}
}
