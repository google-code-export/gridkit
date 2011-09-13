package org.gridkit.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

public interface Indexable {
    Analyzer getAnalyzer();
    Document getDocument();
    Term getKeyTerm();
}
