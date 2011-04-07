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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class VersionedPutProcessor implements EntryProcessor, PortableObject, Serializable {

	private static final long serialVersionUID = 20110402L;
	
	private int targetVersion;
	private Map<Object, Object> values;
	
	public VersionedPutProcessor() {
		// for deserialization
	}
	
	public VersionedPutProcessor(int targetVersion, Map<Object, Object> values) {
		this.targetVersion = targetVersion;
		this.values = values;
	}

	@Override
	public Object process(Entry entry) {
		Object key = entry.getKey();
		Object value = values.get(key);
		ValueContatiner vc = (ValueContatiner) entry.getValue();
		if (vc == null && value == null) {
			return null;
		}
		else if (vc == null){
			vc = new ValueContatiner();
		}
		vc.addVersion(targetVersion, value);
		entry.setValue(vc);
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map processAll(Set entries) {
		for (Entry entry: (Set<Entry>) entries) {
			process(entry);
		}
		return Collections.EMPTY_MAP;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader in) throws IOException {
		targetVersion = in.readInt(1);
		values = in.readMap(2, new HashMap());
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeInt(1, targetVersion);
		out.writeObject(2, values);
	}
}
