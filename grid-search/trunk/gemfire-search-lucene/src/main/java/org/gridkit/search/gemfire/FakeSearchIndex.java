package org.gridkit.search.gemfire;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class FakeSearchIndex {

	RAMDirectory directory = new RAMDirectory();
	
	public FakeSearchIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
		IndexWriter iw = new IndexWriter(directory, iwc);
		for(int i = 0; i != 10; ++i) {
			Document doc = new Document();
			doc.add(new Field("search", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
			doc.add(new Field(LuceneIndexManager.DOCUMENT_KEY, KeyCodec.objectToString(Integer.valueOf(i)), Store.YES, Index.NOT_ANALYZED_NO_NORMS, TermVector.NO));
			iw.addDocument(doc);
		}
		iw.commit();
		IndexSearcher searcher = new IndexSearcher(directory);
		LuceneIndexManager.getInstance().setSearcherForRegion("fake", searcher);
	}
	
}
