package org.gridkit.search.lucene;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;

public interface LuceneSearchEngine {

	public void insertDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException;

	public void updateDocument(Object key, IndexableDocument doc)  throws CorruptIndexException, LockObtainFailedException, IOException;

	public void deleteDocument(Object key) throws CorruptIndexException, IOException;
	
	public IndexSearcher acquireSearcher() throws CorruptIndexException, IOException;
	
	public void releaseSearcher(IndexSearcher seracher);
}
