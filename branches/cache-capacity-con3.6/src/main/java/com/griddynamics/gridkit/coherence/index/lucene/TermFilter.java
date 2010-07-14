package com.griddynamics.gridkit.coherence.index.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * @author Alexander Solovyov
 */

public class TermFilter extends LuceneBasedFilter {

    private String termValue;

    public TermFilter(String termValue) {
        this.termValue = termValue;
    }

    @Override
    protected Query getFilter() {
        return new TermQuery(new Term(LuceneMapIndex.VALUE, termValue));
    }

    public boolean evaluate(Object o) {
        throw new UnsupportedOperationException();
    }
}
