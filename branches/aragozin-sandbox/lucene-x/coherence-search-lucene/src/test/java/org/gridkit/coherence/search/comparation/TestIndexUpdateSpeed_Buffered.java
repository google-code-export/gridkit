package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

public class TestIndexUpdateSpeed_Buffered extends TestIndexUpdateSpeed {
	
	@Override
	LuceneSearchEngine getSearchEngine() {
		try {
			return new BufferedLucenceSearchEngine();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
}
