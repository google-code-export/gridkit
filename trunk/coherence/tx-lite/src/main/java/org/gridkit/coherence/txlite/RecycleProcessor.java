package org.gridkit.coherence.txlite;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

public class RecycleProcessor extends AbstractProcessor implements PortableObject, Serializable {

	private static final long serialVersionUID = 20110402L;
	
	private int targetVersion;
	
	public RecycleProcessor() {
		// for deserialization
	}
	
	public RecycleProcessor(int targetVersion) {
		this.targetVersion = targetVersion;
	}
	
	@Override
	public Object process(Entry entry) {
		ValueContatiner cx = (ValueContatiner) entry.getValue();
		if (cx != null) {
			cx.sweep(targetVersion);
			if (cx.isEmpty()) {
				entry.remove(false);
			}
			else {
				entry.setValue(cx);
			}
		}
		return null;
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		targetVersion = in.readInt(1);		
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeInt(1, targetVersion);		
	}
}
