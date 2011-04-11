package org.gridkit.coherence.search.lucene;

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class RAMDirectoryProvider implements LuceneDirectoryProvider, Serializable, PortableObject {

	private static final long serialVersionUID = 20100728L;

	@Override
	public Directory createDirectory() {
		return new RAMDirectory();
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
	}
}
