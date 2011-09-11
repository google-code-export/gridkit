package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

public final class ObjectDocument {
    public static final ObjectDocument emptyObjectDocument = new ObjectDocument();

    private final Document document;
    private final Analyzer analyzer;

    public ObjectDocument(Document document, Analyzer analyzer) {
        this.document = document;
        this.analyzer = analyzer;
    }

    public ObjectDocument(Document document) {
        this(document, null);
    }

    public ObjectDocument() {
        this(null, null);
    }

    public Document getDocument() {
        return document;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }
}
