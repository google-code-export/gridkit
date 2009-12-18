/*
 * File: TopicTests.java
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
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.Subscriber;
import com.oracle.coherence.patterns.messaging.Topic;
import com.oracle.coherence.patterns.messaging.TopicSubscriptionConfiguration;

/**
 * <p>Test cases for the {@link Topic} and {@link Topic} {@link Subscriber} implementations.</p>
 * 
 * @author Brian Oliver
 */
public class TopicTests extends MessagingTests {
	
	/**
	 * <p>The number of {@link Topic}s that this test has created thus far.</p>
	 */
	private int nrTopicsCreated = 0;

	
	@Test
	public void autoCommitTest() {
		Identifier topicIdentifier = messagingSession.createTopic("topic-autoCommitTest");
		nrTopicsCreated++;
		
		Subscriber subscriber = messagingSession.subscribe(topicIdentifier);
		
		messagingSession.publishMessage(topicIdentifier, "Hello");
		messagingSession.publishMessage(topicIdentifier, "Gudday");
		messagingSession.publishMessage(topicIdentifier, "Bonjour");
		messagingSession.publishMessage(topicIdentifier, "Howdy");
		
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
	public void nonDurableRollbackTest() {
		Identifier topicIdentifier = messagingSession.createTopic("topic-nonDurableRollbackTest");
		nrTopicsCreated++;
		
		Subscriber subscriber = messagingSession.subscribe(topicIdentifier);
		subscriber.setAutoCommit(false);

		//the following statement should have no effect
		subscriber.rollback();
		
		messagingSession.publishMessage(topicIdentifier, "Hello");
		messagingSession.publishMessage(topicIdentifier, "Gudday");
		messagingSession.publishMessage(topicIdentifier, "Bonjour");
		messagingSession.publishMessage(topicIdentifier, "Howdy");

		//the following statement should have no effect
		subscriber.commit();

		assert "Hello".equals(subscriber.getMessage());
		subscriber.rollback();

		assert "Hello".equals(subscriber.getMessage());
		assert "Gudday".equals(subscriber.getMessage());
		subscriber.rollback();
		
		assert "Hello".equals(subscriber.getMessage());
		assert "Gudday".equals(subscriber.getMessage());
		assert "Bonjour".equals(subscriber.getMessage());
		subscriber.rollback();
		
		assert "Hello".equals(subscriber.getMessage());
		assert "Gudday".equals(subscriber.getMessage());
		assert "Bonjour".equals(subscriber.getMessage());
		assert "Howdy".equals(subscriber.getMessage());
		subscriber.rollback();
		
		subscriber.unsubscribe();
	}
	
	
	@Test
	public void autoCommitDurableSubscriberTest() {
		Identifier topicIdentifier = messagingSession.createTopic("topic-autoCommitDurableSubscriberTest");
		nrTopicsCreated++;
		
		String durableSubscriptionName = "durable-subscription";
		Subscriber durableSubscriber = messagingSession.subscribe(
			topicIdentifier, 
			TopicSubscriptionConfiguration.newDurableConfiguration(durableSubscriptionName)
		);
		
		messagingSession.publishMessage(topicIdentifier, "Hello");

		//the following statement should have no effect
		durableSubscriber.rollback();
		
		assert "Hello".equals(durableSubscriber.getMessage());
		durableSubscriber.release();
		
		messagingSession.publishMessage(topicIdentifier, "Gudday");
		messagingSession.publishMessage(topicIdentifier, "Bonjour");
		messagingSession.publishMessage(topicIdentifier, "Howdy");

		durableSubscriber = messagingSession.subscribe(
			topicIdentifier, 
			TopicSubscriptionConfiguration.newDurableConfiguration(durableSubscriptionName)
		);

		//the following statement should have no effect
		durableSubscriber.commit();

		assert "Gudday".equals(durableSubscriber.getMessage());
		assert "Bonjour".equals(durableSubscriber.getMessage());
		assert "Howdy".equals(durableSubscriber.getMessage());
		
		durableSubscriber.unsubscribe();
	}
	
	
	@Test
	public void manualCommitDurableSubscriberTest() {
		Identifier topicIdentifier = messagingSession.createTopic("topic-manualCommitDurableSubscriberTest");
		nrTopicsCreated++;
		
		String durableSubscriptionName = "durable-subscription";
		Subscriber durableSubscriber = messagingSession.subscribe(
			topicIdentifier, 
			TopicSubscriptionConfiguration.newDurableConfiguration(durableSubscriptionName)
		);
		durableSubscriber.setAutoCommit(false);
		
		messagingSession.publishMessage(topicIdentifier, "Hello");

		//the following statement should have no effect
		durableSubscriber.commit();
		
		assert "Hello".equals(durableSubscriber.getMessage());
		durableSubscriber.commit();
		durableSubscriber.release();
		
		messagingSession.publishMessage(topicIdentifier, "Gudday");
		messagingSession.publishMessage(topicIdentifier, "Bonjour");
		messagingSession.publishMessage(topicIdentifier, "Howdy");

		durableSubscriber = messagingSession.subscribe(
			topicIdentifier, 
			TopicSubscriptionConfiguration.newDurableConfiguration(durableSubscriptionName)
		);
		durableSubscriber.setAutoCommit(false);

		//the following statement should have no effect
		durableSubscriber.commit();

		assert "Gudday".equals(durableSubscriber.getMessage());
		assert "Bonjour".equals(durableSubscriber.getMessage());
		assert "Howdy".equals(durableSubscriber.getMessage());
		durableSubscriber.rollback();
		
		assert "Gudday".equals(durableSubscriber.getMessage());
		assert "Bonjour".equals(durableSubscriber.getMessage());
		assert "Howdy".equals(durableSubscriber.getMessage());
		durableSubscriber.commit();
		
		durableSubscriber.unsubscribe();
	}
	
	
	@Test
	public void concurrentPublisherTest() {
		Identifier topicIdentifier = messagingSession.createTopic("topic-concurrentPublisherTest");
		nrTopicsCreated++;

		Subscriber subscriber = messagingSession.subscribe(topicIdentifier);
		
		Runnable publisher = new Runnable() {
			public void run() {
				for(int i = 0; i < 10; i++)
					messagingSession.publishMessage("topic-concurrentPublisherTest", new Integer(i));
			}
		};
	
		Thread publisherThread = new Thread(publisher);
		publisherThread.setDaemon(true);
		publisherThread.start();
		
		int count = 0;
		while (count < 10) {
			assert new Integer(count++).equals(subscriber.getMessage());
		}
	}
	
	
	@Test
	public void concurrentSubscriberTest() throws InterruptedException {
		Identifier topicIdentifier = messagingSession.createTopic("topic-concurrentSubscriberTest");
		nrTopicsCreated++;

		final int nrMessages = 10;
		final int nrSubscribers = 10;
	
		Runnable subscriberRunnable = new Runnable() {
			public void run() {
				Subscriber subscriber = messagingSession.subscribe("topic-concurrentSubscriberTest");
				long lastMessageNumberConsumed = -1;
				for (int messagesToConsume = nrMessages * nrSubscribers; messagesToConsume > 0; messagesToConsume--) {
					long messageNumber = (Long)subscriber.getMessage();
					assert lastMessageNumberConsumed < messageNumber;
					lastMessageNumberConsumed = messageNumber;
				}
				subscriber.unsubscribe();
			}
		};
		
		ArrayList<Thread> subscriberThreads = new ArrayList<Thread>(nrSubscribers);
		for (int i = 1; i <= nrSubscribers; i++) {
			Thread subscriberThread = new Thread(subscriberRunnable);
			subscriberThread.setName("Subscriber Thread " + i);
			subscriberThread.setDaemon(true);
			subscriberThread.start();
			subscriberThreads.add(subscriberThread);
		}
		
		//we wait a bit for the subscribers to start (including subscribe)
		Thread.sleep(5000);
		
		for(int i = 1; i <= nrMessages * nrSubscribers; i++)
			messagingSession.publishMessage(topicIdentifier, new Long(i));

		for(int i = 0; i < nrSubscribers; i++)
			subscriberThreads.get(i).join();
	}


	@Test 
	public void randomTest() throws InterruptedException {
		final int NR_MESSAGES_TO_PUBLISH = 100;
		final int NR_PUBLISHER_THREADS = 10;
		final int NR_SUBSCRIBER_THREADS = 10;
		final int MAX_PUBLISHER_WAIT_TIME_MS = 200;
		final int MAX_SUBSCRIBER_WAIT_TIME_MS = 500;
		
		final AtomicLong remainingPublishers = new AtomicLong(NR_PUBLISHER_THREADS);
		final AtomicLong nextMessageNumber = new AtomicLong(1);
		final Random random = new Random();
		
		messagingSession.createTopic("topic-randomTest");
		nrTopicsCreated++;

		//------------------
		//setup subscribers
		Runnable subscriberRunnable = new Runnable() {
			public void run() {
				System.out.printf("Commenced %s\n", Thread.currentThread().getName());
				Subscriber subscriber = messagingSession.subscribe("topic-randomTest");
				LinkedList<Long> receivedMessages = new LinkedList<Long>(); 
				
				Long message;
				try {
					do {
						Thread.sleep(random.nextInt(MAX_SUBSCRIBER_WAIT_TIME_MS));
		
						message = (Long)subscriber.getMessage();
						//System.out.printf("%s recieved %s\n", Thread.currentThread().getName(), message);
		
						//add the message received to a set that we can confirm in order delivery
						if (message != null) {
							receivedMessages.add(message);
						}
					} while (message != null);
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}
				subscriber.unsubscribe();
				
				assert receivedMessages.size() == NR_MESSAGES_TO_PUBLISH;
				System.out.printf("Completed %s.  Received %d Messages\n", Thread.currentThread().getName(), receivedMessages.size());
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
		
		Thread.sleep(2000);
		
		//------------------
		//setup publishers
		Runnable publisherRunnable = new Runnable() {
			public void run() {
				System.out.printf("Commenced %s\n", Thread.currentThread().getName());
				try {
					for(int i = 1; i <= NR_MESSAGES_TO_PUBLISH / NR_PUBLISHER_THREADS; i++) {
						messagingSession.publishMessage("topic-randomTest", nextMessageNumber.getAndIncrement());
						
						Thread.sleep(random.nextInt(MAX_PUBLISHER_WAIT_TIME_MS));
					}
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
				}

				//if this is the last publisher, publish a null so each subscriber will terminate
				if (remainingPublishers.decrementAndGet() == 0) 
					messagingSession.publishMessage("topic-randomTest", null);
				System.out.printf("Completed %s\n", Thread.currentThread().getName());
			}
		};
		
		for(int i = 1; i <= NR_PUBLISHER_THREADS; i++) {
			Thread publisherThread = new Thread(publisherRunnable);
			publisherThread.setName(String.format("Publisher #%d", i));
			publisherThread.setDaemon(true);
			publisherThread.start();
		}

		for(int i = 0; i < NR_SUBSCRIBER_THREADS; i++)
			subscriberThreads.get(i).join();
	}
	
	
	@Test
	public void topicIterationTest() {
		int count = 0;
		for(Identifier identifier : messagingSession.getTopicIdentifiers()) {
			count++;
			System.out.printf("Topic %s\n", identifier);
		}
		
		assert count == nrTopicsCreated;
	}
}
