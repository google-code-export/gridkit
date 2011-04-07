/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.ValueUpdater;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryAggregator;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class EntryAggregatorAdapter implements EntryAggregator, PortableObject, Serializable  {

	private static final long serialVersionUID = 20110407L;

	protected int version;
	protected EntryAggregator nestedAgent;
	
	public EntryAggregatorAdapter() {
		// for deserialization
	}
	
	public EntryAggregatorAdapter(EntryAggregator agent, int version) {
		this.nestedAgent = agent;
		this.version = version;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object aggregate(Set set) {
		for(Object obj : set) {
			if (obj instanceof Entry && ((Entry)obj) instanceof ValueContatiner) {
				set = transformSet(set);
				break;
			}
		}
		return nestedAgent.aggregate(set);
	}
	
	@SuppressWarnings("unchecked")
	private Set transformSet(Set set) {
		Set result = new HashSet(set.size());
		for (Object obj : set) {
			result.add(new EntryWrapper((Entry) obj));
		}
		return result;
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
			throw new UnsupportedOperationException("Read only ");
		}

		@Override
		public void setValue(Object value, boolean synthetic) {
			throw new UnsupportedOperationException("Read only ");
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException("Read only ");
		}

		@Override
		public void update(ValueUpdater updater, Object oValue) {
			throw new UnsupportedOperationException("Read only ");
		}

		@Override
		public Object extract(ValueExtractor extractor) {
			Object object = getValue();
			return extractor.extract(object);
		}
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		nestedAgent = (EntryAggregator) in.readObject(1);
		version = in.readInt(2);
		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeObject(1, nestedAgent);
		out.writeInt(2, version);
	}
}
