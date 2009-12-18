/*
 * File: QueueTests.java
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
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.BrokenQueueSubscriber;
import com.oracle.coherence.patterns.messaging.DefaultSubscriptionConfiguration;
import com.oracle.coherence.patterns.messaging.Queue;
import com.oracle.coherence.patterns.messaging.Subscriber;
import com.oracle.coherence.patterns.messaging.SubscriptionConfiguration;

/**
 * <p>Test cases for the {@link Queue} and {@link Queue} {@link Subscriber} implementations.</p>
 * 
 * @author Brian Oliver
 */
public class QueueTests extends MessagingTests {
	
	/**
	 * <p>The number of {@link Queue}s that this test has created thus far.</p>
	 */
	private int nrQueuesCreated = 0;

	
	@Test
	public void autoCommitSingleSubscriberTest() {
		Identifier queueIdentifier = messagingSession.createQueue("queue-autoCommitSingleSubscriberTest");
		nrQueuesCreated++;
		
		Subscriber subscriber = messagingSession.subscribe(queueIdentifier);
		
		messagingSession.publishMessage(queueIdentifier, "Hello");
		messagingSession.publishMessage(queueIdentifier, "Gudday");
		messagingSession.publishMessage(queueIdentifier, "Bonjour");
		messagingSession.publishMessage(queueIdentifier, "Howdy");
		
		assert "Hello".equals(subscriber.getMessage());

		//the following statement should have no effect
		subscriber.rollback();
		
		assert "Gudday".equals(subscriber.getMessage());

		//the following statement should have no effect
		subscriber.commit();

		assert "Bonjour".equals(subscriber.getMessage());
		assert "Howdy".equals(subscriber.getMessage());
		
		subscriber.unsubscribe();
	}

	
	@Test
	public void manualCommitSingleSubscriberTest() {
		Identifier queueIdentifier = messagingSession.createQueue("queue-manualCommitSingleSubscriberTest");
		nrQueuesCreated++;
		
		Subscriber subscriber = messagingSession.subscribe(queueIdentifier);
		subscriber.setAutoCommit(false);

		//the following statement should have no effect
		subscriber.rollback();
		
		messagingSession.publishMessage(queueIdentifier, "Hello");
		
		assert "Hello".equals(subscriber.getMessage());
		subscriber.commit();
		
		messagingSession.publishMessage(queueIdentifier, "Gudday");
		messagingSession.publishMessage(queueIdentifier, "Bonjour");
		messagingSession.publishMessage(queueIdentifier, "Howdy");
		subscriber.rollback();

		//the following statement should have no effect
		subscriber.commit();
		
		assert "Gudday".equals(subscriber.getMessage());
		assert "Bonjour".equals(subscriber.getMessage());
		assert "Howdy".equals(subscriber.getMessage());
		subscriber.commit();
		
		subscriber.unsubscribe();
	}
	
	
	@Test
	public void autoCommitingMultipleSubscriberTest() {
		Identifier queueIdentifier = messagingSession.createQueue("queue-autoCommitingMultipleSubscriberTest");
		nrQueuesCreated++;

		Subscriber subscriber1 = messagingSession.subscribe(queueIdentifier);
		Subscriber subscriber2 = messagingSession.subscribe(queueIdentifier);
		
		messagingSession.publishMessage(queueIdentifier, "Hello");
		messagingSession.publishMessage(queueIdentifier, "Gudday");

		//the following statement should have no effect
		subscriber1.commit();
	
		//the following statement should have no effect
		subscriber2.rollback();

		assert "Hello".equals(subscriber1.getMessage());
		assert "Gudday".equals(subscriber2.getMessage());

		//the following statement should have no effect
		subscriber2.commit();
		
		messagingSession.publishMessage(queueIdentifier, "Bonjour");
		messagingSession.publishMessage(queueIdentifier, "Howdy");
		messagingSession.publishMessage(queueIdentifier, "Hi");

		assert "Bonjour".equals(subscriber2.getMessage());
		assert "Howdy".equals(subscriber1.getMessage());
	
		subscriber1.unsubscribe();
		subscriber2.unsubscribe();

		Subscriber subscriber3 = messagingSession.subscribe(queueIdentifier);
		assert "Hi".equals(subscriber3.getMessage());
		
		subscriber3.unsubscribe();
	}
	
	
	@Test
	public void concurrentPublisherTest() {
		Identifier queueIdentifier = messagingSession.createQueue("queue-concurrentPublisherTest");
		nrQueuesCreated++;
		
		Runnable publisher = new Runnable() {
			public void run() {
				for(int i = 1; i <= 10; i++)
					messagingSession.publishMessage("queue-concurrentPublisherTest", new Integer(i));
			}
		};
		
		Thread publisherThread = new Thread(publisher);
		publisherThread.setDaemon(true);
		publisherThread.start();
		
		Subscriber subscriber = messagingSession.subscribe(queueIdentifier);
		int count = 1;
		while (count <= 10) {
			assert new Integer(count++).equals(subscriber.getMessage());
		}
	}
	
	
	@Test
	public void concurrentSubscriberTest() throws InterruptedException {
		Identifier queueIdentifier = messagingSession.createQueue("queue-concurrentSubscriberTest");
		nrQueuesCreated++;

		final int nrMessages = 10;
		final int nrSubscribers = 10;
		
		Runnable subscriberRunnable = new Runnable() {
			public void run() {
				Subscriber subscriber = messagingSession.subscribe("queue-concurrentSubscriberTest");
				long lastMessageNumberConsumed = -1;
				for (int messagesToConsume = nrMessages; messagesToConsume > 0; messagesToConsume--) {
					long messageNumber = (Long)subscriber.getMessage();
					assert lastMessageNumberConsumed < messageNumber;
					lastMessageNumberConsumed = messageNumber;
				}
				subscriber.unsubscribe();
			}
		};
		
		ArrayList<Thread> subscriberThreads = new ArrayList<Thread>(nrSubscribers);
		for (int i = 1; i <= nrSubscribers; i++) {
			Thread consumerThread = new Thread(subscriberRunnable);
			consumerThread.setName("Subscriber Thread " + i);
			consumerThread.setDaemon(true);
			consumerThread.start();
			subscriberThreads.add(consumerThread);
		}
		
		for(int i = 1; i <= nrMessages * nrSubscribers; i++)
			messagingSession.publishMessage(queueIdentifier, new Long(i));

		for(int i = 0; i < nrSubscribers; i++)
			subscriberThreads.get(i).join();
	}
	
	
	@Test 
	public void randomTest() throws InterruptedException {
		final int NR_MESSAGES_TO_PUBLISH = 1000;
		final int NR_PUBLISHER_THREADS = 10;
		final int NR_SUBSCRIBER_THREADS = 10;
		final int MAX_PUBLISHER_WAIT_TIME_MS = 200;
		final int MAX_SUBSCRIBER_WAIT_TIME_MS = 500;
		
		final AtomicLong remainingPublishers = new AtomicLong(NR_PUBLISHER_THREADS);
		final AtomicLong nextMessageNumber = new AtomicLong(1);
		final ConcurrentLinkedQueue<Long> receivedMessages = new ConcurrentLinkedQueue<Long>();
		final Random random = new Random();
		
		messagingSession.createQueue("queue-randomTest");
		nrQueuesCreated++;
		
		//------------------
		//setup publishers
		Runnable publisherRunnable = new Runnable() {
			public void run() {
				System.out.printf("Commenced %s\n", Thread.currentThread().getName());
				try {
					for(int i = 1; i <= NR_MESSAGES_TO_PUBLISH / NR_PUBLISHER_THREADS; i++) {
						messagingSession.publishMessage("queue-randomTest", nextMessageNumber.getAndIncrement());
						
						Thread.sleep(random.nextInt(MAX_PUBLISHER_WAIT_TIME_MS));
					}
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}

				//if this is the last publisher, publish null's for each subscriber to terminate them
				if (remainingPublishers.decrementAndGet() == 0) {
					for (int i = 1; i <= NR_SUBSCRIBER_THREADS; i++) {
						messagingSession.publishMessage("queue-randomTest", null);
					}
				}
				System.out.printf("Completed %s\n", Thread.currentThread().getName());
			}
		};
		
		for(int i = 1; i <= NR_PUBLISHER_THREADS; i++) {
			Thread publisherThread = new Thread(publisherRunnable);
			publisherThread.setName(String.format("Publisher #%d", i));
			publisherThread.setDaemon(true);
			publisherThread.start();
		}

		//------------------
		//setup subscribers
		Runnable subscriberRunnable = new Runnable() {
			public void run() {
				System.out.printf("Commenced %s\n", Thread.currentThread().getName());
				Subscriber subscriber = messagingSession.subscribe("queue-randomTest");
				Long message;
				try {
					do {
						Thread.sleep(random.nextInt(MAX_SUBSCRIBER_WAIT_TIME_MS));
		
						message = (Long)subscriber.getMessage();
		
						//add the message received to a set that we can confirm in order delivery
						if (message != null) {
							receivedMessages.add(message);
						}
					} while (message != null);
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
				subscriber.unsubscribe();
				System.out.printf("Completed %s\n", Thread.currentThread().getName());
			}
		};
		
		ArrayList<Thread> subscriberThreads = new ArrayList<Thread>(NR_SUBSCRIBER_THREADS);
		for (int i = 1; i <= NR_SUBSCRIBER_THREADS; i++) {
			Thread consumerThread = new Thread(subscriberRunnable);
			consumerThread.setName(String.format("Subscriber #%d", i));
			consumerThread.setDaemon(true);
			consumerThread.start();
			subscriberThreads.add(consumerThread);
		}

		for(int i = 0; i < NR_SUBSCRIBER_THREADS; i++)
			subscriberThreads.get(i).join();
		
		assert receivedMessages.size() == nextMessageNumber.get()-1;
		System.out.printf("Received %d Messages\n", nextMessageNumber.get()-1);
	}
	
	
	@Test
	public void terminatingQueueSubscriberTest() throws InterruptedException {
		Identifier queueIdentifier = messagingSession.createQueue("queue-terminatingQueueSubscriberTest");
		nrQueuesCreated++;
				
		messagingSession.publishMessage(queueIdentifier, "Hello");
		messagingSession.publishMessage(queueIdentifier, "Gudday");
		messagingSession.publishMessage(queueIdentifier, "Bonjour");
		messagingSession.publishMessage(queueIdentifier, "Howdy");
		
		//create a subscription configuration that has a very short lease
		SubscriptionConfiguration shortLeaseSubscriptionConfiguration = new DefaultSubscriptionConfiguration(1500);
		
		//attempt to get a message but fail (internally)
		Subscriber brokenQueueSubscriber = new BrokenQueueSubscriber(
			messagingSession.subscribe(queueIdentifier, shortLeaseSubscriptionConfiguration)
		);
		brokenQueueSubscriber.getMessage();

		//now try a bunch of times with random delays.
		final Random random = new Random();
		final int MAX_BROKEN_PUBLISHER_DELAY = 100;
		for(int i = 1; i < 100; i++) {
			brokenQueueSubscriber = new BrokenQueueSubscriber(messagingSession.subscribe(queueIdentifier, shortLeaseSubscriptionConfiguration));
			brokenQueueSubscriber.getMessage();			
			Thread.sleep(random.nextInt(MAX_BROKEN_PUBLISHER_DELAY));
		}
		
		//now consumer all of the messages
		//(due to rollbacks occurring above, the order of messages may no longer be the same)
		//(this happens in all messaging systems... rollbacks can break ordering)
		Subscriber subscriber = messagingSession.subscribe(queueIdentifier);
		System.out.printf("Message = %s\n", subscriber.getMessage());
		System.out.printf("Message = %s\n", subscriber.getMessage());
		System.out.printf("Message = %s\n", subscriber.getMessage());
		System.out.printf("Message = %s\n", subscriber.getMessage());
		subscriber.unsubscribe();
	}
	
	
	@Test
	public void queueIterationTest() {
		int count = 0;
		for(Identifier identifier : messagingSession.getQueueIdentifiers()) {
			count++;
			System.out.printf("Queue %s\n", identifier);
		}
		
		assert count == nrQueuesCreated;
	}
}
