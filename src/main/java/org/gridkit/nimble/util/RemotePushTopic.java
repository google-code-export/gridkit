package org.gridkit.nimble.util;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gridkit.nimble.platform.PushTopic;

@SuppressWarnings("serial")
public class RemotePushTopic<M> implements PushTopic<M>, Serializable {
    
	private RemoteTopic<M> hub = new Hub<M>();
    private transient Thread pushThread;
    private transient BlockingQueue<M> publishQueue;
	
    @Override
    public void subscribe(final Subscriber<M> subscriber) {
        hub.subscribe(new RemoteSubscriber<M>() {			
			@Override
			public void push(Collection<M> msgs) {
				subscriber.push(msgs);				
			}
		});
    }

    @Override
    public synchronized void publish(Collection<M> msgs) {
    	if (pushThread == null) {
    		publishQueue = new ArrayBlockingQueue<M>(4 << 10);
    		Thread t = new Thread(new Runnable() {				
				@Override
				public void run() {
					pushToHub();					
				}
			});
    		t.setDaemon(true);
    		t.setName("PushTopic.Publisher-" + t.getName());
    		t.start();
    	}
    	publishQueue.addAll(msgs);
    }
    
    @Override
	public void sync() throws InterruptedException {
		while(publishQueue.size() > 0) {
			Thread.sleep(1000);
		}
	}

	private void pushToHub() {
    	List<M> buffer = new ArrayList<M>(2 << 10);
    	try {
			while(true) {
				M first = publishQueue.take();
				buffer.add(first);
				publishQueue.drainTo(buffer, (2 << 10) - 1);
				hub.publish(buffer);
				buffer.clear();
			}
		} catch (InterruptedException e) {			
		}
    	synchronized (this) {
			pushThread = null;
			publishQueue = null;
		}
    }
    
    private static class Hub<M> implements RemoteTopic<M> {
    	
    	private List<Subscriber<M>> subscribers = new CopyOnWriteArrayList<Subscriber<M>>();
    	private BlockingQueue<M> buffer = new ArrayBlockingQueue<M>(8 << 10); 
    	
        @Override
        public void subscribe(Subscriber<M> subscriber) {
            subscribers.add(subscriber);
        }

        @Override
        public void publish(Collection<M> msgs) {
        	buffer.addAll(msgs);
        	synchronized (this) {
        		List<M> data = new ArrayList<M>();
        		buffer.drainTo(data);
        		
        		List<Subscriber<M>> shuffledSubscribers = new ArrayList<Subscriber<M>>(subscribers);
        		
        		Collections.shuffle(shuffledSubscribers);
        		
        		for (Subscriber<M> subscriber : shuffledSubscribers) {
        			subscriber.push(msgs);
        		}
			}
        }

		@Override
		public void sync() {
			// do nothing
		}    	
    }
    
    public interface RemoteTopic<M> extends PushTopic<M>, Remote {    	
    }
    
    public interface RemoteSubscriber<M> extends PushTopic.Subscriber<M>, Remote {    	
    }
}
