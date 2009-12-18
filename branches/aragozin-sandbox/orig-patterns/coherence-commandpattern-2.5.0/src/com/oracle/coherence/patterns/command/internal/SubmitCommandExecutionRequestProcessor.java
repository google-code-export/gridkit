/*
 * File: SubmitCommandExecutionRequestProcessor.java
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
package com.oracle.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>A {@link SubmitCommandExecutionRequestProcessor} is used to submit {@link Command}s
 * (represented as {@link CommandExecutionRequest}s) to a {@link CommandExecutor} for
 * execution.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class SubmitCommandExecutionRequestProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {
	
	/**
	 * <p>The {@link CommandExecutionRequest} to submit for execution.</p>
	 */
	private CommandExecutionRequest commandExecutionRequest;

	
	/**
	 * <p>Signifies that a {@link CommandExecutionRequest} may be accepted (ie: submitted)
	 * when the associated {@link Context} has yet to be registered.  This permits 
	 * {@link Command}s to be submitted before their {@link Context}s are created.</p>
	 */
	private boolean acceptCommandIfContextDoesNotExist;
	

	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SubmitCommandExecutionRequestProcessor() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param commandExecutionRequest
	 * @param acceptIfContextDoesNotExist
	 */
	public SubmitCommandExecutionRequestProcessor(CommandExecutionRequest commandExecutionRequest,
											  	  boolean acceptIfContextDoesNotExist) {
		this.commandExecutionRequest = commandExecutionRequest;
		this.acceptCommandIfContextDoesNotExist = acceptIfContextDoesNotExist;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {
		//we need the CommandExecutor so that we can submit the CommandExecutionRequest for execution
		CommandExecutor commandExecutor;
		Identifier contextIdentifier = commandExecutionRequest.getContextIdentifier();
		if (acceptCommandIfContextDoesNotExist) {
			//TODO: The following line needs to be changed when we adopt Coherence 3.5.2 to use the BinaryEntry.getContext()
			BackingMapManagerContext backingMapManagerContext = 
				((DistributedCacheService)CacheFactory.getService("DistributedCacheForCommandPattern")).
					getBackingMapManager().getContext();
			
			commandExecutor = CommandExecutorManager.ensureCommandExecutor(contextIdentifier, backingMapManagerContext);
		} else {
			commandExecutor = CommandExecutorManager.getCommandExecutor(contextIdentifier);
		}

		//only accept the command execution request if there is a command executor
		if (commandExecutor == null && !acceptCommandIfContextDoesNotExist) {
			return new SubmissionOutcome.UnknownContext();

		} else {
			//accept the command execution request for execution
			return commandExecutor.acceptCommandExecutionRequest(commandExecutionRequest);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.commandExecutionRequest = (CommandExecutionRequest)ExternalizableHelper.readExternalizableLite(in);
		this.acceptCommandIfContextDoesNotExist = in.readBoolean();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeExternalizableLite(out, commandExecutionRequest);
		out.writeBoolean(acceptCommandIfContextDoesNotExist);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.commandExecutionRequest = (CommandExecutionRequest)reader.readObject(0);
		this.acceptCommandIfContextDoesNotExist = reader.readBoolean(1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, commandExecutionRequest);
		writer.writeBoolean(1, acceptCommandIfContextDoesNotExist);
	}
}


