package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.oracle.coherence.common.ticketing.Ticket;
import com.tangosol.net.CacheFactory;

class MessageOrderingSupport {
	
	private static AtomicReference<Sequence> sequence;	
	
	public static Ticket nextTicket() {
		Sequence sq = sequence.get();
		if (sq == null) {
			sequence.set(new Sequence());
			sq = sequence.get();
		}
		
		Ticket tck = sq.nextTicket();
		if (tck.getSequenceNumber() > 100 && tck.getIssuerId() > CacheFactory.getSafeTimeMillis()) {
			sequence.set(new Sequence());
		}
		return tck;
	}
	
	private static class Sequence {
		long lastMilliTime;
		AtomicInteger counter = new AtomicInteger();
		
		public Sequence() {
			lastMilliTime = CacheFactory.getSafeTimeMillis();
		}
		
		public Ticket nextTicket() {
			return new Ticket(lastMilliTime, counter.get());
		}
	}

}
