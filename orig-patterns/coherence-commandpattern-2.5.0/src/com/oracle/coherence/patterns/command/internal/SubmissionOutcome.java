/*
 * File: SubmissionOutcome.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
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
package com.oracle.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.oracle.coherence.patterns.command.internal.CommandExecutionRequest.Key;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link SubmissionOutcome} represents the result of
 * an attempt to submit a {@link Command} to a {@link Context}
 * (via a {@link CommandExecutor} using a {@link CommandExecutionRequest}).
 * </p>
 * 
 * @see SubmitCommandExecutionRequestProcessor
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class SubmissionOutcome implements ExternalizableLite, PortableObject {
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SubmissionOutcome() {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
	}
	
	
	/**
	 * <p>An {@link Accepted} {@link SubmissionOutcome} represents
	 * that a {@link Command} has been successfully submitted 
	 * to a {@link Context} and is awaiting execution by a 
	 * {@link CommandExecutor}.</p>
	 */
	public static class Accepted extends SubmissionOutcome implements Identifier {
				
		/**
		 * <p>The {@link Key} that was created for the {@link Command}
		 * that was submitted.  This may be used (by clients) to
		 * cancel execution of a pending {@link Command}.</p>
		 */
		private CommandExecutionRequest.Key commandExecutionRequestKey;
		
		
		/**
		 * <p>The {@link ManagementStrategy} that was used to store
		 * the accepted {@link Command}.</p>
		 */
		private ManagementStrategy managementStrategy;
		
		
		/**
		 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
		 */
		public Accepted() {
		}
	
		
		/**
		 * <p>Standard Constructor.</p>
		 * 
		 * @param commandExecutionRequestKey
		 */
		public Accepted(CommandExecutionRequest.Key commandExecutionRequestKey,
						ManagementStrategy managementStrategy) {
			this.commandExecutionRequestKey = commandExecutionRequestKey;
			this.managementStrategy = managementStrategy;
		}
		
		
		/**
		 * <p>Returns the {@link Key} that was created for the submitted
		 * {@link Command}.</p>
		 */
		public CommandExecutionRequest.Key getCommandExecutionRequestKey() {
			return commandExecutionRequestKey;
		}

		
		/**
		 * <p>Returns the {@link ManagementStrategy} that was used to store
		 * the {@link Command}.</p>
		 */
		public ManagementStrategy getManagementStrategy() {
			return managementStrategy;
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			super.readExternal(in);
			this.commandExecutionRequestKey = (CommandExecutionRequest.Key)ExternalizableHelper.readObject(in);
			this.managementStrategy = ManagementStrategy.valueOf(ExternalizableHelper.readSafeUTF(in));
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			super.writeExternal(out);
			ExternalizableHelper.writeObject(out, commandExecutionRequestKey);
			ExternalizableHelper.writeSafeUTF(out, this.managementStrategy.toString());
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			super.readExternal(reader);
			this.commandExecutionRequestKey = (CommandExecutionRequest.Key)reader.readObject(100);
			this.managementStrategy = ManagementStrategy.valueOf(reader.readString(101));
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			super.writeExternal(writer);
			writer.writeObject(100, commandExecutionRequestKey);
			writer.writeString(101, managementStrategy.toString());
		}
	}


	/**
	 * <p>An {@link UnknownContext} {@link SubmissionOutcome} represents
	 * that a {@link Command} has failed being submitted to a
	 * {@link Context} as the said {@link Context} does not exist.</p>
	 */
	public static class UnknownContext extends SubmissionOutcome {
		
		/**
		 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
		 */
		public UnknownContext() {
		}
	}
}
