package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.ValueUpdater;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;

public class EntryProcessorAdapter implements EntryProcessor, PortableObject, Serializable  {

	private int version = Integer.MAX_VALUE;
	private boolean readOnly = true;
	private EntryProcessor nestedProcessor;
	
	public EntryProcessorAdapter() {
		// for deserialization
	}
	
	public EntryProcessorAdapter(EntryProcessor processor, int version, boolean readOnly) {
		this.nestedProcessor = processor;
		this.version = version;
		this.readOnly = readOnly;
	}

	@Override
	public Object process(Entry entry) {
		return nestedProcessor.process(new EntryWrapper(entry));
	}

	@Override
	public Map processAll(Set entries) {
		Set<Entry> eset = new HashSet<Entry>(entries.size());
		for(Object e: entries) {
			eset.add(new EntryWrapper((Entry) e));
		}
		return nestedProcessor.processAll(eset);
	}
	
	private class EntryWrapper implements Entry {
		
		private final Entry entry;
		
		public EntryWrapper(Entry entry) {
			this.entry = entry;
		}

		@Override
		public Object getKey() {
			return entry.getKey();
		}

		@Override
		public Object getValue() {
			ValueContatiner vc = (ValueContatiner) entry.getValue();
			return vc == null ? null : vc.getVersionAt(version);
		}

		@Override
		public boolean isPresent() {
			return entry.isPresent();
		}

		@Override
		public void remove(boolean synthetic) {
			if (readOnly) {
				throw new UnsupportedOperationException("Cache connection is read only ");
			}
			else {
				ValueContatiner vc = (ValueContatiner) entry.getValue();
				if (vc != null) {
					vc.addVersion(version, null);
				}
			}
		}

		@Override
		public void setValue(Object value, boolean synthetic) {
			if (readOnly) {
				throw new UnsupportedOperationException("Cache connection is read only ");
			}
			else {
				ValueContatiner vc = (ValueContatiner) entry.getValue();
				if (vc == null) {
					vc = new ValueContatiner();
					entry.setValue(vc, false);
				}
				vc.addVersion(version, value);
			}
		}

		@Override
		public Object setValue(Object value) {
			if (readOnly) {
				throw new UnsupportedOperationException("Cache connection is read only ");
			}
			else {
				ValueContatiner vc = (ValueContatiner) entry.getValue();
				if (vc == null) {
					vc = new ValueContatiner();
					entry.setValue(vc, false);
				}
				Object oldValue = vc.getVersionAt(version);
				vc.addVersion(version, value);
				return oldValue;
			}
		}

		@Override
		public void update(ValueUpdater updater, Object oValue) {
			Object oTarget = entry.getValue();
			updater.update(oTarget, oValue);
			entry.setValue(oTarget, false);
		}

		@Override
		public Object extract(ValueExtractor extractor) {
			Object object = getValue();
			return extractor.extract(object);
		}
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	
}
