package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oracle.coherence.common.ticketing.Ticket;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;

public class SubmitMessageProcessor extends AbstractMobileObject implements EntryProcessor {
	
	private static final long serialVersionUID = 20100111L;
	
	private Map<Object, List<Pack>> commandsTree = new HashMap<Object, List<Pack>>();
	
	public SubmitMessageProcessor() {		
	};
	
	/**
	 * @param context
	 * @param uid
	 * @param coloc
	 * @param command either object or Binary if command was early serialized
	 */
	public void addCommand(Object context, long uid, Object command) {
		List<Pack> chain = commandsTree.get(context);
		if (chain == null) {
			chain = new ArrayList<Pack>();
			commandsTree.put(context, chain);
		}		
		chain.add(new Pack(context, uid, command));		
	}

	@Override
	public Object process(Entry entry) {
		return processAll(Collections.singleton(entry));
	}

	@Override
	public Map processAll(Set setEntries) {
		Map<Object, Object> result = new HashMap<Object, Object>();
		Map<Long, Entry> refKeys = new HashMap<Long, Entry>();
		Map<Long, Entry> bodyKeys = new HashMap<Long, Entry>();
		
		for(Object o: setEntries) {
			Object key = ((Entry)o).getKey();
			if (key instanceof CommandRefKey) {
				long uid = ((CommandRefKey)key).msgUid;
				refKeys.put(uid, ((Entry)o));
			}
			else if (key instanceof CommandBodyKey) {
				long uid = ((CommandBodyKey)key).msgUid;
				bodyKeys.put(uid, ((Entry)o));
			}
		}
		
		for(List<Pack> chain: commandsTree.values()) {
			for(Pack pack: chain) {
				long uid = pack.msgUid;
				Entry ref = refKeys.get(uid);
				Entry body = bodyKeys.get(uid);
				if (body != null && pack.message != null) {
					if (pack.message instanceof Binary) {
						((BinaryEntry)body).updateBinaryValue((Binary) pack.message);
					}
					else {
						body.setValue(pack.message);
					}
					result.put(body.getKey(), Boolean.TRUE);
				}
				if (ref != null) {
					Ticket tck = MessageOrderingSupport.nextTicket();
					CommandRef cmdRef = new CommandRef(pack.contextKey, uid, tck.getIssuerId(), (int)tck.getSequenceNumber());
					body.setValue(cmdRef);
					CommandQueues.enqueue(cmdRef);
					result.put(ref.getKey(), tck);
				}
			}
		}
		
		return result;
	}
	
	@Override
	protected void readWireFormat(WireFormatIn in) throws IOException {
		commandsTree = new HashMap<Object, List<Pack>>();
		while(true) {
			Object contextKey = in.readObject();
			if (contextKey == null) {
				break;
			}
			int len = in.readInt();
			Pack[] pack = new Pack[len];
			for(int i = 0; i != pack.length; ++i) {
				long uid = in.readLong();
				Object msg = in.readObject();
				pack[i] = new Pack(contextKey, uid, msg);
			}
			commandsTree.put(contextKey, Arrays.asList(pack));
		}
		
	}

	@Override
	protected void writeWireFormat(WireFormatOut out) throws IOException {
		for(List<Pack> chain: commandsTree.values()) {
			Object contextKey = chain.get(0).contextKey;
			out.writeObject(contextKey);
			out.writeInt(chain.size());
			for(Pack pack: chain) {
				out.writeLong(pack.msgUid);
				out.writeObject(pack.message);
			}
		}
		out.writeObject(null);
	}

	static class Pack {
		Object contextKey;
		long msgUid;
		Object message;
		
		public Pack(Object contextKey, long msgUid, Object message) {
			this.contextKey = contextKey;
			this.msgUid = msgUid;
			this.message = message;
		}
	}
}
