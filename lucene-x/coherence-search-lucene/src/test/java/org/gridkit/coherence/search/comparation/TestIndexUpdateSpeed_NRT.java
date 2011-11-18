package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class TestIndexUpdateSpeed_NRT extends TestIndexUpdateSpeed {
	
	@Override
	LuceneSearchEngine getSearchEngine() {
		return new NaiveLuceneSearchEngine();
	}
	
	public class NaiveLuceneSearchEngine implements LuceneSearchEngine {

		RAMDirectory dir = new RAMDirectory();
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
		IndexWriter writer;
		IndexSearcher searcher;
		
		int uncommited = 0;
		
		@Override
		public void insertDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException {
			touchWriter();
			doc.getDocument().add(new Field("#DOC_KEY#", key.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
			doc.addToIndex(writer);
			uncommited++;
			if (uncommited > 100) {
				writer.commit();
			}
		}

		private void touchWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
			if (writer == null) {
				writer = new IndexWriter(dir,iwc);
			}
			if (searcher != null) {
				searcher.close();
			}
			searcher = null;
		}

		@Override
		public void updateDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException {
			touchWriter();
			writer.deleteDocuments(new Term("#DOC_KEY#", key.toString()));
			doc.getDocument().add(new Field("#DOC_KEY#", key.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));
			doc.addToIndex(writer);
		}

		@Override
		public void deleteDocument(Object key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IndexSearcher acquireSearcher() throws CorruptIndexException, IOException {
			if (searcher == null) {
//				if (writer != null) {
//					writer.;
//				}
				searcher = new IndexSearcher(IndexReader.open(writer, true));
			}
			return searcher;
		}

		@Override
		public void releaseSearcher(IndexSearcher seracher) {
		}

		@Override
		public String toString() {
			return "NRT + RAM (size " + dir.sizeInBytes() + ")";
		}
	}
}
