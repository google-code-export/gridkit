package com.oracle.coherence.patterns.messaging;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.exceptions.SubscriberInterruptedException;
import com.oracle.coherence.patterns.messaging.exceptions.SubscriptionLostException;

/**
 * <p>A {@link Subscriber} provides mechanisms to retreive and acknowledge
 * allocated {@link Message}s from a {@link Destination}.</p>
 * 
 * <p>{@link Subscriber} instances are created and managed through the use
 * of a {@link MessagingSession}.</p>
 * 
 * @author Brian Oliver
 */
public interface Subscriber {

	/**
	 * <p>Returns the {@link MessagingSession} that created the {@link Subscriber}.</p>
	 */
	public MessagingSession getMessagingSession();

	
	/**
	 * <p>Returns the {@link Identifier} of the {@link Subscription}
	 * created and used by the {@link Subscriber}.</p>
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier();

	
	/**
	 * <p>Returns the {@link Identifier} for the {@link Destination} to which 
	 * this {@link Subscriber} is subscribed.</p>
	 */
	public Identifier getDestinationIdentifier();
	
	
	/**
	 * <p>Returns if the {@link Subscriber} may be used to receive messages.</p>
	 */
	public boolean isActive();
	
		
	/**
	 * <p>Unsubscribes the {@link Subscription} for the {@link Subscriber} from the 
	 * associated {@link Destination} and releases any necessary resources.</p>
	 * 
	 * <p>NOTE 1: After making a call to this method the {@link Subscriber} and it's associated
	 * {@link Subscription} can not be used to receive messages.</p>
	 * 
	 * <p>NOTE 2: Calling this method on a durable {@link Topic} {@link Subscription} will result
	 * in the {@link Subscription} being removed.</p>
	 * 
	 * @throws SubscriptionLostException
	 */
	public void unsubscribe();
	
	
	/**
	 * <p>Releases the {@link Subscriber} and any associated resources from the
	 * {@link Destination}.  If the {@link Subscriber} {@link Subscription} has
	 * been been configured as being durable (using a {@link TopicSubscriptionConfiguration}),
	 * the underlying {@link Subscription} is not removed.  However, if the 
	 * {@link Subscriber} is non-durable, the underlying {@link Subscription} is
	 * also removed, making this method semantically equivalent to calling {@link #unsubscribe()}.</p>
	 * 
	 * <p>NOTE: After making a call to this method the {@link Subscriber} can not be used any further.</p>
	 * 
	 * @see TopicSubscriptionConfiguration
	 * 
	 * @throws SubscriptionLostException
	 */
	public void release();
	
	
	/**
	 * <p>Sets whether messages received (via {@link #getMessage()}) will 
	 * automatically be acknowledged for the {@link Subscriber}.</p>
	 * 
	 * <p>The default is set to <code>true</code>
	 *
	 * <p>NOTE: changing this value to <code>true</code> when there are 
	 * uncommitted messages for this {@link Subscriber} will automatically 
	 * rollback the said messages.</p>
	 * 
	 * @param autoCommit
	 * 
	 * @throws SubscriptionLostException
	 */
	public void setAutoCommit(boolean autoCommit);
	
	
	/**
	 * <p>Returns if the {@link Subscriber} will auto-commit (ie: acknowledge) any messages
	 * it receives using {@link #getMessage()}.</p>
	 */
	public boolean isAutoCommitting();

	
	/**
	 * <p>Requests and waits (indefinitely) for a {@link Message} to be 
	 * delivered to the {@link Subscriber}.</p>
	 * 
	 * <p>If {@link #isAutoCommitting()}, once a {@link Message} has arrived,
	 * it is marked as being acknowledged and thus can't be rolled back onto the {@link Destination}.</p>
	 * 
	 * @throws SubscriberInterruptedException
	 * @throws SubscriptionLostException
	 */
	public Object getMessage();
	
	
	/**
	 * <p>Commits any received messages when using {@link #getMessage()}.</p>
	 * 
	 * <p>NOTE: Calls to this method are only useful when not {@link #isAutoCommitting()}.</p>
	 * 
	 * @throws SubscriptionLostException
	 */
	public void commit();
	
	
	/**
	 * <p>Places messages delivered to the {@link Subscriber} (but not acknowledged)
	 * back onto the front of the {@link Destination} to which the {@link Subscriber}
	 * is subscribed, in the order received.</p>
	 * 
	 * <p>NOTE: Calls to this method are only useful when not {@link #isAutoCommitting()}.</p>
	 * 
	 * @throws SubscriptionLostException
	 */
	public void rollback();
	
}
