package org.gridkit.coherence.events;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.Base;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public abstract class TriggerToBMListenerAdapter implements MapTrigger, PortableObject {

	private static final long serialVersionUID = 20121214L;
	
	private volatile MapListener listener;

	protected abstract MapListener instantiateListener(String cacheName, BackingMapManagerContext context);
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
	}

	@Override
	public void process(Entry entry) {
		try {
			BinaryEntry be = ((BinaryEntry)entry);
			if (listener == null) {
				synchronized (this) {
					if (listener == null) {
						String cacheName = be.getBackingMapContext().getCacheName();
						listener = instantiateListener(cacheName, be.getContext());
					}
				}
			}
			dispatch(listener, be);
		}
		catch(Exception e) {
			CacheFactory.log("TriggerToBMListenerAdapter: " + e.toString(), Base.LOG_WARN);
		}
	}

	private void dispatch(MapListener listener, BinaryEntry entry) {
		if (entry.getOriginalBinaryValue() == null) {
			MapEvent me = new MapEvent(entry.getBackingMap(), MapEvent.ENTRY_INSERTED, entry.getBinaryKey(), entry.getOriginalBinaryValue(), entry.getBinaryValue());
			listener.entryInserted(me);
		}
		else if (entry.getBinaryValue() == null) {
			MapEvent me = new MapEvent(entry.getBackingMap(), MapEvent.ENTRY_DELETED, entry.getBinaryKey(), entry.getOriginalBinaryValue(), entry.getBinaryValue());
			listener.entryDeleted(me);
		}
		else {
			MapEvent me = new MapEvent(entry.getBackingMap(), MapEvent.ENTRY_UPDATED, entry.getBinaryKey(), entry.getOriginalBinaryValue(), entry.getBinaryValue());
			listener.entryUpdated(me);
		}
	}
}
