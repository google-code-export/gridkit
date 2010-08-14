package org.gridkit.coherence.search.lucene;

import java.io.Serializable;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class RAMDirectoryProvider implements LuceneDirectoryProvider, Serializable {

	private static final long serialVersionUID = 20100728L;

	@Override
	public Directory createDirectory() {
		return new RAMDirectory();
	}
}
