package org.gridkit.search.lucene;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface IndexableDocument {

	public Document getDocument();
	
	public void addToIndex(IndexWriter indexWriter) throws CorruptIndexException, IOException;
	
}
