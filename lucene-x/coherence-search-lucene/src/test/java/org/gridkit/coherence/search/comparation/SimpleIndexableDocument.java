package org.gridkit.coherence.search.comparation;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

public class SimpleIndexableDocument implements IndexableDocument {

	private Document document;
	
	public SimpleIndexableDocument(Document document) {
		this.document = document;
	}
	
	@Override
	public Document getDocument() {
		return document;
	}

	@Override
	public void addToIndex(IndexWriter writer) throws CorruptIndexException, IOException {
		writer.addDocument(document);
	}

}
