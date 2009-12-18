/*
 * File: FilePublisher.java
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
package com.oracle.coherence.patterns.pushreplication.publishers;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A simple {@link Publisher} to publish {@link EntryOperation}s to
 * a file.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class FilePublisher extends AbstractPublisher {

	/**
	 * <p>The name of the directory (that must exist) to which the
	 * {@link FilePublisher} will write results.</p>
	 */
	private String directoryName;
	
	
	/**
	 * <p>If the file should be opened for appending.</p>
	 */
	private boolean isAppending;
	
	
	/**
	 * <p>The {@link BufferedWriter} that will be used for writing
	 * to the file.</p>
	 */
	private transient BufferedWriter bufferedWriter;
	

	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public FilePublisher() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param directoryName
	 * @param isAppending
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public FilePublisher(String directoryName, 
						 boolean isAppending, 
						 long restartDelay,
						 int totalConsecutiveFailuresBeforeSuspending) {
		super(restartDelay, totalConsecutiveFailuresBeforeSuspending);
		this.directoryName = directoryName;
		this.isAppending = isAppending;
		this.bufferedWriter = null;
	}

	
	/**
	 * <p>Standard Constructor (permitting an indefinite number of publishing failures).</p>
	 * 
	 * @param directoryName
	 * @param isAppending
	 * @param restartDelay
	 */
	public FilePublisher(String directoryName, 
						 boolean isAppending, 
						 long restartDelay) {
		this(directoryName, isAppending, restartDelay, -1);
	}
	
	
	/**
	 * <p>Returns the name of the directory (it must exist) in which the 
	 * file publishing will write files.</p>
	 */
	public String getDirectoryName() {
		return directoryName;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void publish(EntryOperation entryOperation) {
		try {
			bufferedWriter.write(String.format("%s, %s, %s, %s\n",
											   entryOperation.getCacheName(),
											   entryOperation.getOperation(), 
											   entryOperation.getKey(),
											   entryOperation.getValue()));
			bufferedWriter.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace(System.err);
			throw new RuntimeException(ioException);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		try {
			String fileName = String.format("%s-%d.log", subscriptionIdentifier, System.currentTimeMillis());
			bufferedWriter = new BufferedWriter(new FileWriter(new File(directoryName, fileName), isAppending));
			
		} catch (IOException ioException) {
			ioException.printStackTrace(System.err);
			bufferedWriter = null;
			throw new RuntimeException(ioException);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionHandle) {
		if (bufferedWriter != null) {
			try {
				bufferedWriter.close();
			} catch (IOException ioException) {
				ioException.printStackTrace(System.err);
			} finally {
				bufferedWriter = null;
			}
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.directoryName = ExternalizableHelper.readSafeUTF(in);
		this.isAppending = in.readBoolean();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeSafeUTF(out, directoryName);
		out.writeBoolean(isAppending);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.directoryName = reader.readString(100);
		this.isAppending = reader.readBoolean(101);
	}


	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeString(100, directoryName);
		writer.writeBoolean(101, isAppending);
	}
}
