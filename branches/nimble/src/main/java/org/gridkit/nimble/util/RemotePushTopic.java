package org.gridkit.nimble.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gridkit.nimble.platform.PushTopic;

public class RemotePushTopic<M> implements PushTopic<M> {
    private List<Subscriber<M>> subscribers = new CopyOnWriteArrayList<Subscriber<M>>();
    
    @Override
    public void subscribe(Subscriber<M> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void publish(Collection<M> msgs) {
        List<Subscriber<M>> shuffledSubscribers = new ArrayList<Subscriber<M>>(subscribers);
        
        Collections.shuffle(shuffledSubscribers);
        
        for (Subscriber<M> subscriber : shuffledSubscribers) {
            subscriber.push(msgs);
        }
    }
}
