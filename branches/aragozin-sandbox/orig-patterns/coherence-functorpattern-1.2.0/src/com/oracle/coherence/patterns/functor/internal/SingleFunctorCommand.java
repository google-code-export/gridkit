/*
 * File: SingleFunctorCommand.java
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
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.functor.Functor;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;

/**
 * <p>A {@link SingleFunctorCommand} is a {@link Command} that asynchronously 
 * executes a single {@link Functor} and returns the result.</p>
 * 
 * @author Brian Oliver
 *
 * @param <C>
 * @param <T>
 */
@SuppressWarnings("serial")
public class SingleFunctorCommand<C extends Context, T> implements Command<C>, ExternalizableLite, PortableObject {

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
	 * <p>The {@link Functor} that will be executed when this {@link Command} is executed.</p>
	 */
	private Functor<C, T> functor;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SingleFunctorCommand() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param functorSubmitterIdentifier
	 * @param functorResultIdentifier
	 * @param functor
	 */
	public SingleFunctorCommand(Identifier functorSubmitterIdentifier,
								  Identifier functorResultIdentifier, 
								  Functor<C, T> functor) {
		this.functorSubmitterIdentifier = functorSubmitterIdentifier;
		this.functorResultIdentifier = functorResultIdentifier;
		this.functor = functor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<C> executionEnvironment) {
		
		SingleFunctorResult<T> result;
		try {
			T value = functor.execute(executionEnvironment);
			result = new SingleFunctorResult<T>(functorSubmitterIdentifier, functorResultIdentifier, value, null);
			
		} catch (Exception exception) {
			Logger.log(Logger.WARN, 
					   "Failed to execute Functor %s with Ticket %s in Context %s as it raised the following exception\n%s\n", 
					   functor,
					   executionEnvironment.getTicket(),
					   executionEnvironment.getContext(),
					   exception);
			
			result = new SingleFunctorResult<T>(functorSubmitterIdentifier, functorResultIdentifier, null, exception);			
		}
		
		//place the result in the results cache (if it's not present)
		NamedCache functorResultsCache = CacheFactory.getCache(FunctorResult.CACHENAME);
		functorResultsCache.invoke(functorResultIdentifier, new ConditionalPut(new NotFilter(PresentFilter.INSTANCE), result));
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(DataInput input) throws IOException {
		this.functorSubmitterIdentifier = (Identifier)ExternalizableHelper.readObject(input);
		this.functorResultIdentifier = (Identifier)ExternalizableHelper.readObject(input);
		this.functor = (Functor<C, T>)ExternalizableHelper.readObject(input);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput output) throws IOException {
		ExternalizableHelper.writeObject(output, functorSubmitterIdentifier);
		ExternalizableHelper.writeObject(output, functorResultIdentifier);
		ExternalizableHelper.writeObject(output, functor);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		this.functorSubmitterIdentifier = (Identifier)reader.readObject(0);
		this.functorResultIdentifier = (Identifier)reader.readObject(1);
		this.functor = (Functor<C, T>)reader.readObject(2);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, functorSubmitterIdentifier);
		writer.writeObject(1, functorResultIdentifier);
		writer.writeObject(2, functor);
	}
}
