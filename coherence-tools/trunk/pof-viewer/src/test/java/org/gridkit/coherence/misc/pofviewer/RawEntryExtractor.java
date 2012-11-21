package org.gridkit.coherence.misc.pofviewer;

import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * Simple processor extracting raw binary from Coherence cache.
 *  
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class RawEntryExtractor extends AbstractProcessor implements Serializable, PortableObject {

	private static final long serialVersionUID = 20121121L;

	@Override
	public Object process(Entry entry) {
		BinaryEntry be = (BinaryEntry) entry;
		Binary key = be.getBinaryKey();
		Binary value = be.getBinaryValue();
		return new Binary[]{key, value};
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		// nothing
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		// nothing
	}
}
