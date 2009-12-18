/*
 * File: PublishingService.java
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
package com.oracle.coherence.patterns.pushreplication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.ranges.Ranges;
import com.oracle.coherence.common.threading.ThreadFactories;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.messaging.entryprocessors.AcknowledgeSubscriptionMessagesProcessor;
import com.oracle.coherence.patterns.messaging.entryprocessors.DrainSubscriptionMessagesProcessor;
import com.oracle.coherence.patterns.messaging.entryprocessors.RemoveSubscriberFromMessageProcessor;
import com.oracle.coherence.patterns.pushreplication.EntryOperationBatch.EntryOp;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.management.Registry;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.GreaterEqualsFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.processor.ExtractorProcessor;

/**
 * <p>A {@link PublishingService} is responsible for
 * managing the publication (ie: push) of {@link EntryOperation}s for
 * an given {@link PublishingSubscription} using an 
 * associated {@link BatchPublisher}.</p>
 * 
 * <p>NOTE: A {@link PublishingService} only works in "batch" mode.  
 * Support for publishing individual {@link EntryOperation}s, one-at-a-time
 * with {@link Publisher} implementations is provided by the 
 * BatchPublisherAdapter (that internally uses a batch size of 1 
 * and a batch delay of 0)</p>
 * 
 * <p>Internally a {@link PublishingService} operates as a 
 * finite-state-machine based around the life-cycle of a 
 * {@link BatchPublisher}, including how to recover in the event of failures.</p>
 *
 * @author Brian Oliver
 */
public class PublishingService implements PublishingServiceMBean {

	/**
	 * <p>The {@link Identifier} of the {@link PublishingSubscription}
	 * that will be used to create a {@link Publisher} and
	 * where we're up to when publishing.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;
	
	
	/**
	 * <p>The {@link BatchPublisher} that will be used to publish
	 * batches of {@link EntryOperation}s for this {@link PublishingService}.</p>
	 */
	private BatchPublisher batchPublisher;
	
	
	/**
	 * <p>A {@link ScheduledExecutorService} that will be used to 
	 * manage the life-cycle of the {@link PublishingService}.</p>
	 */
	private ScheduledExecutorService executorService;
	
	
	/**
	 * <p>The {@link Range} of {@link EntryOperation}s that was last published
	 * by this {@link PublishingService}.</p>
	 */
	private Range lastPublishedRange;
	
	
	/**
	 * <p>The time (in ms) the last publishing batch took to publish.</p>
	 */
	private long lastPublishingDuration;
	
	
	/**
	 * <p>The minimum time (in ms) a batch has taken to publish.</p> 
	 */
	private long minimumPublishingDuration;

	
	/**
	 * <p>The maximum time (in ms) a batch has taken to publish.</p> 
	 */
	private long maximumPublishingDuration;

	
	/**
	 * <p>The total time (in ms) the batches published so far have taken
	 * to publish.</p> 
	 */
	private long totalPublishingDuration;
	
	
	/**
	 * <p>The number of consecutive failures that have occurred thus far
	 * without success.</p>
	 */
	private int consecutivePublishingFailures;
	
	
	/**
	 * <p>The name on the MBean associated with this service</p>
	 */
	private String mBeanName;	
	
	
	/**
	 * <p>The {@link State} of the {@link PublishingService} life-cycle.</p>
	 */
	public static enum State {
		/**
		 * <p><code>Suspended</code> indicates that the {@link PublishingService}
		 * is not operational.  It means that the {@link PublishingService}
		 * has failed too many times, has not started (ie: autostart = false), or 
		 * has been manually suspended.  Once in this state no publishing will occur 
		 * until it is manually restarted (typically via JMX).</p>
		 */
		Suspended,
		
		/**
		 * <p><code>Paused</code> indicates that the {@link PublishingService}
		 * has not been started or has previously failed.  It means that the 
		 * {@link PublishingService} is scheduled to be restarted at some time in the 
		 * future.  This is the starting state for a {@link PublishingService}.</p>
		 */
		Paused,
		
		/**
		 * <p><code>Starting</code> indicates that the {@link PublishingService}
		 * is in the process of starting.</p>
		 */
		Starting,
		
		/**
		 * <p><code>Ready</code> indicates that the {@link PublishingService} is
		 * ready to publish messages, but has not been scheduled to do so.</p>
		 */
		Ready,
		
		/**
		 * <p><code>Waiting</code> indicates that the {@link PublishingService} is
		 * waiting for the next batch publish cycle. ie: it has been scheduled
		 * to publish.</p>
		 */
		Waiting,
		
		/**
		 * <p><code>Publishing</code> indicates that the {@link PublishingService} is
		 * currently in the process of publishing {@link EntryOperation}s using
		 * the {@link BatchPublisher}.</p>
		 */
		Publishing,
		
		/**
		 * <p><code>Stopping</code> indicates that the {@link PublishingService} is 
		 * in the process of stopping publishing, most likely because it is shutting down</p>
		 */
		Stopping,
		
		/**
		 * <p><code>Stopped</code> indicates that the {@link PublishingService} is
		 * stopped and can't be restarted.</p>
		 */
		Stopped;
	};
	
	
	/**
	 * <p>The current {@link State} of the {@link PublishingService}.</p>
	 */
	private State state;

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param batchPublisher
	 */
	public PublishingService(SubscriptionIdentifier subscriptionIdentifier,
							 BatchPublisher batchPublisher) {
		this.subscriptionIdentifier = subscriptionIdentifier;
		this.batchPublisher = batchPublisher;
		this.executorService = Executors.newSingleThreadScheduledExecutor(ThreadFactories.newThreadFactory(true, "PublishingService", new ThreadGroup("PublishingService")));
		this.state = batchPublisher.autostart() ? State.Paused : State.Suspended;
		this.lastPublishedRange = Ranges.EMPTY;
		this.lastPublishingDuration = 0;
		this.maximumPublishingDuration = Long.MIN_VALUE;
		this.minimumPublishingDuration = Long.MAX_VALUE;
		this.totalPublishingDuration = 0;
		this.consecutivePublishingFailures = 0;
	}
	
	
	/**
	 * <p>Returns the {@link Identifier} of the {@link PublishingSubscription}
	 * that this {@link PublishingService} will be using to publish
	 * {@link EntryOperation}s.</p>
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}

	
	/**
	 * <p>Returns the {@link BatchPublisher} the {@link PublishingService} will
	 * use to publish {@link EntryOperation}s.</p>
	 */
	public BatchPublisher getBatchPublisher() {
		return batchPublisher;
	}
	

	/**
	 * <p>Sets the {@link State} of the {@link PublishingService}.</p>
	 * 
	 * @param state
	 */
	synchronized void setState(State state) {
		this.state = state;
	}
	
	
	/**
	 * <p>Returns the current {@link State} of the {@link PublishingService}.</p>
	 */
	synchronized State getState() {
		return state;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String getSubscriptionIdentity() {
		return subscriptionIdentifier.toString();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String getServiceState() {
		return state.toString();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String getLastPublishedRange() {
		return lastPublishedRange.toString();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public long getLastPublishingDuration() {
		return lastPublishingDuration;
	}	

	
	/**
	 * {@inheritDoc}
	 */
	public long getMaximumPublishingDuration() {
		return maximumPublishingDuration == Long.MIN_VALUE ? 0 : maximumPublishingDuration;
	}	

	
	/**
	 * {@inheritDoc}
	 */
	public long getMinimumPublishingDuration() {
		return minimumPublishingDuration == Long.MAX_VALUE ? 0 : minimumPublishingDuration;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public long getTotalPublishingDuration() {
		return totalPublishingDuration;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int getConsecutivePublishingFailures() {
		return consecutivePublishingFailures;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void suspend() {
		synchronized(this) {
			State currentState = getState();
			if (currentState == State.Publishing || 
				currentState == State.Ready || 
				currentState == State.Waiting || 
				currentState == State.Paused) {
				
				if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
					CacheFactory.log(String.format("Suspending PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);
				
				setState(State.Suspended);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void resume() {
		synchronized(this) {
			if (getState() == State.Suspended) {
				
				if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
					CacheFactory.log(String.format("Resuming PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);

				//reset the number of consecutive failures - let's start our count again!
				consecutivePublishingFailures = 0;
				
	 			setState(State.Starting);
	 			schedule(new Runnable() {
	 				public void run() {
	 					start();
	 				}
	 			}, 0, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void drain() {
		synchronized(this) {
			if (getState() == State.Suspended) {
				
				if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
					CacheFactory.log(String.format("Draining PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);

				//NOTE: A better way to achieve this would be to submit a DrainTopicSubscription "control" message
				//		to the Topic.  This way it would also survive in the instance that the JVM executing this dies 
				//	    after draining but before removing the subscription from the messages.
				
				//determine the Range of messages to delete
				NamedCache subscriptionsCache = CacheFactory.getCache(Subscription.CACHENAME);
				Range messageIdRange = (Range)subscriptionsCache.invoke(subscriptionIdentifier, 
														         	    new DrainSubscriptionMessagesProcessor());		

				if (messageIdRange != null) {
					//remove the messages that fall in the range
					NamedCache messagesCache = CacheFactory.getCache(Message.CACHENAME);	
					messagesCache.invokeAll(
							new AndFilter(
								new GreaterEqualsFilter("getMessageId", messageIdRange.getFrom()), 
								new LessEqualsFilter("getMessageId", messageIdRange.getTo())),
							new RemoveSubscriberFromMessageProcessor(subscriptionIdentifier));
				}

				if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
					CacheFactory.log(String.format("Drained message range %s for PublishingService %s", messageIdRange, subscriptionIdentifier), CacheFactory.LOG_DEBUG);
				
			} else {
				CacheFactory.log(String.format("Can't drain the PublishingService for %s as it is not Suspended.  Please suspend first before attempting to drain.", subscriptionIdentifier), CacheFactory.LOG_WARN);
			}
		} 
	}
	
	
	/**
	 * <p>Sets the name of the mBean that will be used when 
	 * this service is registered with JMX</p>
	 */
	public void setMBeanName(String mBeanName){
		this.mBeanName = mBeanName;
	}
	
	
	/**
	 * <p>Returns the name of the mBean that will be used when 
	 * this service is registered with JMX.</p>
	 */
	public String getMBeanName(){
		return mBeanName;
	}
	
	
	/**
	 * <p>Schedules a {@link Runnable} to be executed by the {@link PublishingService}.
	 * This is typically used to schedule starting and stopping of the said {@link PublishingService}.</p>
	 * 
	 * @param runnable
	 * @param delay
	 * @param timeUnit
	 */
	void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
		if (getState() != State.Stopped) {
			executorService.schedule(runnable, delay, timeUnit);
		}
	}
	
	
	/**
	 * <p>Starts the {@link PublishingService} by attempting to start the {@link Publisher}
	 * and place the said {@link PublishingService} into {@link State#Waiting} mode, after which
	 * it schedules to start publishing.</p>
	 */
	public void start() {
		if (getState() == State.Paused || getState() == State.Starting) {
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
				CacheFactory.log(String.format("Starting PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);
			
			//attempt to start the underlying publisher
			try {
				//start the publisher
				batchPublisher.start(subscriptionIdentifier);
				
				setState(State.Ready);
				
				//we now schedule publishing to commence immediately
				//so that any unpublished work gets started as soon as 
				//the publisher is available.
				final PublishingService self = this;
				schedule(new Runnable() {
					public void run() {
						self.publish();
					}
				}, 0, TimeUnit.MILLISECONDS);
				
			} catch (RuntimeException runtimeException) {
				CacheFactory.log(String.format("Failed while attempting to start the PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_WARN);
				runtimeException.printStackTrace(System.err);

				//we've now had a failure!
				consecutivePublishingFailures++;
				
				//schedule a retry?
				if (batchPublisher.getTotalConsecutiveFailuresBeforeSuspending() < 0 ||
					consecutivePublishingFailures < batchPublisher.getTotalConsecutiveFailuresBeforeSuspending()) {
					//when an error occurs, we go back to the paused state, and schedule a restart (retry) after the restart delay
					setState(State.Paused);
					schedule(new Runnable() {
						public void run() {
							start();
						}
					}, batchPublisher.getRestartDelay(), TimeUnit.MILLISECONDS);
					
				} else {
					//suspend publishing as we've had too many consecutive failures
					CacheFactory.log(String.format("Suspending publishing for subscription %s as there have been too many (%d) consecutive failures ", subscriptionIdentifier, consecutivePublishingFailures), CacheFactory.LOG_WARN);
					setState(State.Suspended);
				}
			}
		}
	}
	
	
	/**
	 * <p>Stops the {@link PublishingService}.  Once stopped, it should not be started again.</p>
	 */
	public void stop() {
		
		//we can only stop if we're not already stopped or stopping
		if (getState() != State.Stopped && getState() != State.Stopping) {
			setState(State.Stopping);
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG)) 
				CacheFactory.log(String.format("Stopping PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);
			
			try {
				batchPublisher.stop(subscriptionIdentifier);
			} catch (RuntimeException runtimeException) {
				CacheFactory.log(String.format("Failed while attempting to stop the PublishingService for %s", subscriptionIdentifier), CacheFactory.LOG_WARN);
				runtimeException.printStackTrace(System.err);
				
			} finally {
				setState(State.Stopped);
				
				//unregister the MBean for this PublishingService
				Registry registry = CacheFactory.ensureCluster().getManagement();
				if (registry != null) {
					registry.unregister(getMBeanName());
				}
			}
		}
	}
	
	
	/**
	 * <p>Starts the {@link PublishingService} to publish {@link EntryOperation}s
	 * in the {@link Subscription} with the {@link Publisher}.</p>
	 */
	@SuppressWarnings("unchecked")
	void publish() {
		if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
			CacheFactory.log(String.format("Commenced publishing for subscription %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);
		
		//we can only start publishing if we are Waiting or Ready
		if (getState() == State.Waiting || getState() == State.Ready) {	
			
			//we're now in publishing mode
			setState(State.Publishing);
			
			//continue to publish while we're in publishing mode
			while(getState() == State.Publishing) {
				Range messageRange = Ranges.EMPTY;
				try {
					//get the range of message ids we could possibly publish
					NamedCache subscriptions = CacheFactory.getCache(Subscription.CACHENAME);
					messageRange = (Range)subscriptions.invoke(subscriptionIdentifier, new ExtractorProcessor("getVisibleMessageRange"));
	
					//any messages to publish?
					if (messageRange == null || messageRange.isEmpty()) {
						//as there is nothing to publish so we have to go back into the Ready state
						//(there is no need to schedule a batch as there is nothing to batch!)
						setState(State.Ready);
						
					} else {
						//we need to get the messages from somewhere :P
						NamedCache messagesCache = CacheFactory.getCache(Message.CACHENAME);	

						//publish the entire range of messages (entry operations)
						Iterator<Long> messageIds = messageRange.iterator();
						while (messageIds.hasNext()) {

							//create a batch of message keys and the batch range
							long batchFrom = Long.MAX_VALUE;
							long batchTo = Long.MIN_VALUE;
							ArrayList<String> batchMessageKeys = new ArrayList<String>(batchPublisher.getBatchSize());
							while (messageIds.hasNext() && batchMessageKeys.size() < batchPublisher.getBatchSize()) {
								long messageId = messageIds.next();
								batchFrom = messageId < batchFrom ? messageId : batchFrom;
								batchTo = messageId > batchTo ? messageId : batchTo;
								batchMessageKeys.add(Message.getKey(subscriptionIdentifier.getDestinationIdentifier(), messageId));
							}
							Range batchMessageIdRange = Ranges.newRange(batchFrom, batchTo);
							
							//get all of the messages in our batch
							Map<String, Message> batchMessages = (Map<String, Message>)messagesCache.getAll(batchMessageKeys);
							
							//build our batch of entry operations from the messages (in order of which they should be processed)
							ArrayList<EntryOperation> batchEntryOperations = new ArrayList<EntryOperation>(batchPublisher.getBatchSize());
							for(String batchMessageKey : batchMessageKeys) {
								Message batchMessage = batchMessages.get(batchMessageKey);
								if (batchMessage != null && 
									batchMessage.isVisibleTo(subscriptionIdentifier) &&
									!batchMessage.isAcknowledgedBy(subscriptionIdentifier)) {
									
									Object payload = batchMessage.getPayload();
									if (payload instanceof EntryOperation) {
										batchEntryOperations.add((EntryOperation)payload);
									}else if (payload instanceof EntryOperationBatch) {
										EntryOperationBatch batch = (EntryOperationBatch) payload;
										for (EntryOp op : batch.getEntryBatch()) {
											batchEntryOperations.add(new EntryOperation(batch.getSiteName(),
													                                    batch.getClusterName(),
													                                    batch.getCacheName(),
													                                    op.getOperation(),
													                                    op.getKey(),
													                                    op.getValue()));
										}
									}
								}
							}

							if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
								CacheFactory.log(String.format("Commenced: Publishing Service for %s publishing batch message %s", subscriptionIdentifier, batchMessageIdRange), CacheFactory.LOG_DEBUG);

							long startPublishingTime = System.currentTimeMillis();
							
							//publish this batch of entry operations
							batchPublisher.publishBatch(subscriptionIdentifier, batchEntryOperations.iterator());

							//calculate JMX statistics
							long publishingDuration = System.currentTimeMillis() - startPublishingTime;
							lastPublishingDuration = publishingDuration;
							maximumPublishingDuration = Math.max(maximumPublishingDuration, publishingDuration);
							minimumPublishingDuration = Math.min(minimumPublishingDuration, publishingDuration);
							totalPublishingDuration += publishingDuration;
							
							if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
								CacheFactory.log(String.format("Completed: Publishing Service for %s publishing batch message %s", subscriptionIdentifier, batchMessageIdRange), CacheFactory.LOG_DEBUG);

							//remove this subscriber from each of the messages in this batch 
							//(to help clean up the messages when they are completely consumed)
							messagesCache.invokeAll(batchMessageKeys, new RemoveSubscriberFromMessageProcessor(subscriptionIdentifier));
							
							//remove the range we've just published from the subscriber
							subscriptions.invoke(subscriptionIdentifier, new AcknowledgeSubscriptionMessagesProcessor(batchMessageIdRange));
							
							//remember the range we've just published
							lastPublishedRange = batchMessageIdRange;
							
							//reset the number of consecutive failures as we've had success!
							consecutivePublishingFailures = 0;
						}
						
						//now go back into the Waiting state to wait for the next batch time (if a delay is necessary)
						//we can only do this if we're still in the Publishing state... 
						//ie: we many have been forced into the suspended state!
						if (getState() == State.Publishing && batchPublisher.getBatchPublishingDelay() > 0) {
							setState(State.Waiting);
							
							//schedule the next time we'll attempt to publish a batch
							schedule(new Runnable() {
								public void run() {
									publish();
								}
							}, batchPublisher.getBatchPublishingDelay(), TimeUnit.MILLISECONDS);
						}								
					}
					
				} catch (RuntimeException runtimeException) {
					
					CacheFactory.log(String.format("Failed to publish the range %s of messages for subscription %s with publisher %s.",
									 messageRange, 
									 subscriptionIdentifier,
									 batchPublisher), CacheFactory.LOG_ERR);
					CacheFactory.log(runtimeException);

					//we've now had a failure!
					consecutivePublishingFailures++;
					
					//schedule a retry?
					if (batchPublisher.getTotalConsecutiveFailuresBeforeSuspending() < 0 ||
						consecutivePublishingFailures < batchPublisher.getTotalConsecutiveFailuresBeforeSuspending()) {
						//when an error occurs, we go back to the paused state, and schedule a restart (retry) after the restart delay
						setState(State.Paused);
						schedule(new Runnable() {
							public void run() {
								start();
							}
						}, batchPublisher.getRestartDelay(), TimeUnit.MILLISECONDS);
						
					} else {
						//suspend publishing as we've had too many consecutive failures
						CacheFactory.log(String.format("Suspending publishing for subscription %s as there have been too many (%d) consecutive failures ", subscriptionIdentifier, consecutivePublishingFailures), CacheFactory.LOG_WARN);
						setState(State.Suspended);
					}
				}
			}			
			
		} else {
			//the following logging indicates that we've were once in a Ready/Waiting state and started publishing, but in the mean time
			//the publishing service was stopped, usually due to a re-balancing.  This is perfectly ok as we haven't lost any information
			//we've just moved to another node to do it.
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("PublishingService for subscription %s is not in the Ready or Waiting state (currently in %s)... so skipping publishing", 
											   subscriptionIdentifier,
											   state),
								 CacheFactory.LOG_DEBUG);
		}

		if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
			CacheFactory.log(String.format("Completed publishing for subscription %s", subscriptionIdentifier), CacheFactory.LOG_DEBUG);
	}
}
