package org.gridkit.search.lucene;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

public class SimpleIndexableDocument implements IndexableDocument {

	private Document doc;
	
	public SimpleIndexableDocument(Document doc) {
		this.doc = doc;
	}

	@Override
	public Document getDocument() {
		return doc;
	}

	@Override
	public void addToIndex(IndexWriter indexWriter) throws CorruptIndexException, IOException {
		indexWriter.addDocument(doc);
	}

	@Override
	public String toString() {
		return doc.toString();
	}
}
