/*
 * File: SingleFunctorResult.java
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
package com.oracle.coherence.patterns.functor.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.functor.Functor;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>The {@link SingleFunctorResult} captures the result of executing 
 * a single {@link Functor}.</p>
 * 
 * @author Brian Oliver
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class SingleFunctorResult<T> implements FunctorResult, ExternalizableLite, PortableObject {
	
	/**
	 * <p>The {@link Identifier} of the {@link FunctorSubmitter} that submitted
	 * the {@link Functor} for execution.</p>
	 */
	private Identifier functorSubmitterIdentifier;
	
	
	/**
	 * <p>The {@link Identifier} that will be used to locate the result 
	 * of the {@link Functor} execution in the functor-results cache.</p> 
	 */
	private Identifier functorResultIdentifier;
	
	
	/**
	 * <p>The result value of executing the {@link Functor}.</p>
	 */
	private T value;
	
	
	/**
	 * <p>The (possible) exception thrown during the execution of the {@link Functor}.</p>
	 */
	private Exception exception;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SingleFunctorResult() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param functorSubmitterIdentifier
	 * @param functorResultIdentifier
	 * @param value
	 * @param exception
	 */
	public SingleFunctorResult(Identifier functorSubmitterIdentifier,
				  		 Identifier functorResultIdentifier, 
				  		 T value,
				  		 Exception exception) {
		this.functorSubmitterIdentifier = functorSubmitterIdentifier;
		this.functorResultIdentifier = functorResultIdentifier;
		this.value = value;
		this.exception = exception;
	}
	
	
	/**
	 * <p>Returns the {@link Identifier} for the {@link FunctorSubmitter} that
	 * submitted the {@link Functor} for execution.</p>
	 */
	public Identifier getFunctorSubmitterIdentifier() {
		return functorSubmitterIdentifier;
	}


	/**
	 * <p>Returns the {@link Identifier} that the {@link FunctorSubmitter} will
	 * use to differentiate results from {@link Functor} executions.</p>
	 */
	public Identifier getFunctorResultIdentifier() {
		return functorResultIdentifier;
	}


	/**
	 * <p>Returns the resulting value from executing the {@link Functor}
	 * (may be null).</p>
	 */
	public T getValue() {
		return value;
	}


	/**
	 * <p>Return the {@link Exception} that was (possibly) thrown
	 * during the execution of the {@link Functor} that produced
	 * this result.</p>
	 */
	public Exception getException() {
		return exception;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean isComplete() {
		//The result for a single {@link Functor} execution is always complete.
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(DataInput input) throws IOException {
		this.functorSubmitterIdentifier = (Identifier)ExternalizableHelper.readObject(input);
		this.functorResultIdentifier = (Identifier)ExternalizableHelper.readObject(input);
		this.value = (T)ExternalizableHelper.readObject(input);
		this.exception = (Exception)ExternalizableHelper.readObject(input);			
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput output) throws IOException {
		ExternalizableHelper.writeObject(output, functorSubmitterIdentifier);
		ExternalizableHelper.writeObject(output, functorResultIdentifier);
		ExternalizableHelper.writeObject(output, value);
		ExternalizableHelper.writeObject(output, exception);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		this.functorSubmitterIdentifier = (Identifier)reader.readObject(0);
		this.functorResultIdentifier = (Identifier)reader.readObject(1);
		this.value = (T)reader.readObject(2);
		this.exception = (Exception)reader.readObject(3);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, functorSubmitterIdentifier);
		writer.writeObject(1, functorResultIdentifier);
		writer.writeObject(2, value);
		writer.writeObject(3, exception);
	}
}
