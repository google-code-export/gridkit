/*
 * File: CancelCommandProcessor.java
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
package com.oracle.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.internal.CommandExecutionRequest;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>The {@link CancelCommandProcessor} is responsible for canceling
 * a {@link Command} from executing within a {@link CommandExecutor}.</p>
 *  
 * @author Noah Arliss
 */
@SuppressWarnings("serial")
public class CancelCommandProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}
	 */
	public CancelCommandProcessor() {
	}	
	
	/**
	 * {@inheritDoc} 
	 */
	public Object process(Entry entry) {
		if (entry.isPresent()) {
			CommandExecutionRequest request = (CommandExecutionRequest) entry.getValue();
			if (request.getStatus() == CommandExecutionRequest.Status.Pending) {
				request.setStatus(CommandExecutionRequest.Status.Canceled);
				return true;
			}			
		} 
		return false;
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
}
