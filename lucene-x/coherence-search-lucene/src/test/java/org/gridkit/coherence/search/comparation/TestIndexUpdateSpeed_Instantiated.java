package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.instantiated.InstantiatedIndex;
import org.apache.lucene.store.instantiated.InstantiatedIndexWriter;
import org.apache.lucene.util.Version;

public class TestIndexUpdateSpeed_Instantiated extends TestIndexUpdateSpeed {
	
	@Override
	LuceneSearchEngine getSearchEngine() {
		return new NaiveLuceneSearchEngine();
	}
	
	public class NaiveLuceneSearchEngine implements LuceneSearchEngine {

		InstantiatedIndexWriter writer;
		IndexSearcher searcher;
		InstantiatedIndex ii = new InstantiatedIndex();
		
		int uncommited = 0;
		
		@Override
		public void insertDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException {
			if (writer == null) {
				writer = ii.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
				searcher = null;
			}
			doc.getDocument().add(new Field("#DOC_KEY#", key.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
			writer.addDocument(doc.getDocument());
			writer.commit();
		}

		@Override
		public void updateDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException {
			if (writer == null) {
				writer = ii.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
				searcher = null;
			}
			writer.deleteDocuments(new Term("#DOC_KEY#", key.toString()));
			doc.getDocument().add(new Field("#DOC_KEY#", key.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
			writer.addDocument(doc.getDocument());
			writer.commit();
		}

		@Override
		public void deleteDocument(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IndexSearcher acquireSearcher() throws CorruptIndexException, IOException {
			if (searcher == null) {
				searcher = new IndexSearcher(ii.indexReaderFactory());
			}
			return searcher;
		}

		@Override
		public void releaseSearcher(IndexSearcher seracher) {
		}

		@Override
		public String toString() {
			return "Instantiated";
		}
	}
}
