package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;

public interface LuceneSearchEngine {

	void insertDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException,	 IOException;

	void updateDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException,	 IOException;

	void deleteDocument(Object key) throws CorruptIndexException, IOException;

	IndexSearcher acquireSearcher() throws CorruptIndexException, IOException;

	void releaseSearcher(IndexSearcher searcher);

}
