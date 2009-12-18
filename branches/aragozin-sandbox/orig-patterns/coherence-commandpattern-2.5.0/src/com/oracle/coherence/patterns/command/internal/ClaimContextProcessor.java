/*
 * File: ClaimContextProcessor.java
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
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;
import com.tangosol.util.UID;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>The {@link ClaimContextProcessor} is responsible for "claiming"
 * a {@link Context} for execution with a {@link CommandExecutor}.</p>
 * 
 * <p>This enables us to determine during {@link CommandExecutionRequest} 
 * execute if another thread, possibly on another Member, has also claimed
 * the said {@link ContextWrapper}.  In turn this allows us to fail-fast,
 * and stop processing on the original {@link CommandExecutor} thread
 * to avoid where possible, multiple execution of a {@link Command}.</p>
 *  
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class ClaimContextProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link UID} of the {@link Member} on which we should be claiming the {@link Context}.</p>
	 */
	private UID memberUID;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}
	 */
	public ClaimContextProcessor() {
	}
	

	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param memberUID The {@link UID} of the {@link Member} on which we should be claiming the {@link Context}.
	 */
	public ClaimContextProcessor(UID memberUID) {
		this.memberUID = memberUID;
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public Object process(Entry entry) {
		if (entry.isPresent() && CacheFactory.getCluster().getLocalMember().getUid().equals(memberUID)) {
			ContextWrapper contextWrapper = (ContextWrapper)entry.getValue();
			contextWrapper.nextVersion();
			entry.setValue(contextWrapper);
			return contextWrapper;
		} else {
			return null;
		}
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void readExternal(DataInput in) throws IOException {
		this.memberUID = new UID(in);
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public void writeExternal(DataOutput out) throws IOException {
		memberUID.save(out);
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.memberUID = new UID(reader.readString(0));
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeString(0, memberUID.toString());
	}
}
