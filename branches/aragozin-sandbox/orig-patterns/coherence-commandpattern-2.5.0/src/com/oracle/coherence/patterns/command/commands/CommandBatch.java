/*
 * File: CommandBatch.java
 * 
 * Copyright (c) 2008-2009. All Rights Reserved. Oracle Corporation.
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
import java.util.ArrayList;
import java.util.List;

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link CommandBatch} allows a batch (List) of {@link Command}s to be 
 * submitted to a single {@link Context} for execution in an all or nothing mode.</p>
 * 
 * <p>NOTE: Any exception caused by a {@link Command} in the {@link CommandBatch} will
 * cause the fail-fast of the processing.  {@link Command}s not yet processed will be discarded.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class CommandBatch<C extends Context> implements Command<C>, ExternalizableLite, PortableObject {

	/**
	 * <p>The list of {@link Command}s to be executed.</p>
	 */
	private List<Command<C>> commands;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public CommandBatch() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param commands
	 */
	public CommandBatch(List<Command<C>> commands) {
		this.commands = commands;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<C> executionEnvironment) {
		for(Command<C> command : commands)
			command.execute(executionEnvironment);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		int capacity = ExternalizableHelper.readInt(in);
		if (capacity > 0) {
			commands = new ArrayList<Command<C>>(capacity);
			ExternalizableHelper.readCollection(in, commands, this.getClass().getClassLoader());
		} else {
			commands = new ArrayList<Command<C>>();
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeInt(out, this.commands.size());
		if (commands.size() > 0)
			ExternalizableHelper.writeCollection(out, commands);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		int capacity = reader.readInt(0);
		if (capacity > 0) {
			commands = new ArrayList<Command<C>>(capacity);
			reader.readCollection(1, commands);
		} else {
			commands = new ArrayList<Command<C>>();
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeInt(0, commands.size());
		if (commands.size() > 0)
			writer.writeCollection(1, commands);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("CommandBatch{commands=%s}", commands);
	}
}
