/*
 * File: PublishMessageCommand.java
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
package com.oracle.coherence.patterns.messaging.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.messaging.Destination;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link PublishMessageCommand} is an internal {@link Command} that performs
 * the action of publishing message "payload" to a {@link Destination}, where by
 * the {@link Subscription}(s) will be then notified.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class PublishMessageCommand implements Command<Destination>, ExternalizableLite, PortableObject {

	/**
	 * <p>The payload of the {@link Message}.</p>
	 */
	private Object payload;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public PublishMessageCommand() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param payload
	 */
	public PublishMessageCommand(Object payload) {
		this.payload = payload;
	}


	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<Destination> executionEnvironment) {
		//delegate the implementation to the underlying destination
		executionEnvironment.getContext().publishMessage(executionEnvironment, this);
	}

	
	/**
	 * <p>Returns the payload to be published.</p>
	 */
	public Object getPayload() {
		return payload;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.payload = ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, payload);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.payload = reader.readObject(0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, payload);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("PublishMessageCommand{payload=%s}", payload);
	}
}
