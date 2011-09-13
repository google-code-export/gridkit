package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.gridkit.search.lucene.Indexable;

public final class KeyFieldIndexable implements Indexable {
    private final Document document;
    private final Analyzer analyzer;

    private final String keyFieldName;

    public KeyFieldIndexable(Document document, Analyzer analyzer, String keyFieldName) {
        this.document = document;
        this.analyzer = analyzer;
        this.keyFieldName = keyFieldName;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    @Override
    public Term getKeyTerm() {
        return new Term(keyFieldName, document.getFieldable(keyFieldName).stringValue());
    }
}
