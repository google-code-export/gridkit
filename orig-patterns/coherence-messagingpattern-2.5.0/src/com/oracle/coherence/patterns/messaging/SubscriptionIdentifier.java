/*
 * File: SubscriptionIdentifier.java
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
package com.oracle.coherence.patterns.messaging;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.identifiers.UUIDBasedIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

@SuppressWarnings("serial")
public class SubscriptionIdentifier implements Identifier, ExternalizableLite, PortableObject {
		
	private Identifier destinationIdentifier;
	private Identifier subscriberIdentifier;
	
	/**
	 * <p>Required for {@link ExternalizableLite}.</p>
	 */
	public SubscriptionIdentifier() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationIdentifier
	 * @param subscriberName
	 */
	public SubscriptionIdentifier(Identifier destinationIdentifier, 
				  			  	  String subscriberName) {
		this.destinationIdentifier = destinationIdentifier;
		this.subscriberIdentifier = StringBasedIdentifier.newInstance(subscriberName);
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationIdentifier
	 * @param subscriberIdentifier
	 */
	public SubscriptionIdentifier(Identifier destinationIdentifier, 
								  Identifier subscriberIdentifier) {
		this.destinationIdentifier = destinationIdentifier;
		this.subscriberIdentifier = subscriberIdentifier;
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationIdentifier
	 */
	public SubscriptionIdentifier(Identifier destinationIdentifier) {
		this.destinationIdentifier = destinationIdentifier;
		this.subscriberIdentifier = UUIDBasedIdentifier.newInstance();
	}

	
	/**
	 * <p>Returns the {@link Identifier} of the {@link Destination}
	 * to which the identified subscriber is subscribed.</p> 
	 */
	public Identifier getDestinationIdentifier() {
		return destinationIdentifier;
	}
	
	
	/**
	 * <p>Returns the {@link Identifier} of the subscriber that
	 * is subscribed to the identified {@link Destination}.</p>
	 */
	public Identifier getSubscriberIdentifier() {
		return subscriberIdentifier;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((destinationIdentifier == null) ? 0 : destinationIdentifier
						.hashCode());
		result = prime
				* result
				+ ((subscriberIdentifier == null) ? 0 : subscriberIdentifier.hashCode());
		return result;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SubscriptionIdentifier other = (SubscriptionIdentifier) obj;
		if (destinationIdentifier == null) {
			if (other.destinationIdentifier != null)
				return false;
		} else if (!destinationIdentifier.equals(other.destinationIdentifier))
			return false;
		if (subscriberIdentifier == null) {
			if (other.subscriberIdentifier != null)
				return false;
		} else if (!subscriberIdentifier.equals(other.subscriberIdentifier))
			return false;
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("SubscriptionIdentifier{destinationIdentifier=%s, subscriberIdentifier=%s}", 
							 destinationIdentifier, subscriberIdentifier);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		destinationIdentifier = (Identifier)ExternalizableHelper.readObject(in);
		subscriberIdentifier = (Identifier)ExternalizableHelper.readObject(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, destinationIdentifier);
		ExternalizableHelper.writeObject(out, subscriberIdentifier);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.destinationIdentifier = (Identifier)reader.readObject(0);
		this.subscriberIdentifier = (Identifier)reader.readObject(1);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, destinationIdentifier);
		writer.writeObject(1, subscriberIdentifier);
	}	
}
