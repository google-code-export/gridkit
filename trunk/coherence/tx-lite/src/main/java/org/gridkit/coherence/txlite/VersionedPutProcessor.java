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
	public Map processAll(Set entries) {
		for (Entry entry: (Set<Entry>) entries) {
			process(entry);
		}
		return Collections.EMPTY_MAP;
	}

	@Override
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
