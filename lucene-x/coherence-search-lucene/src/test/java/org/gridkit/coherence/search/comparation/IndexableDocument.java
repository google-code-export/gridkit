package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

public interface IndexableDocument {

	Document getDocument();

	void addToIndex(IndexWriter writer) throws CorruptIndexException, IOException;

}
