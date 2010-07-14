package com.griddynamics.gridkit.coherence.index.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

/**
 * @author Alexander Solovyov
 */

public class WildcardFilter extends LuceneBasedFilter {

    private String wildcard;

    public WildcardFilter(String wildcard) {
        this.wildcard = wildcard;
    }

    protected Query getFilter() {
        return new WildcardQuery(new Term(LuceneMapIndex.VALUE, wildcard));
    }

    public boolean evaluate(Object o) {
        String text = (String) extractor.extract(o);
        return text != null && text.startsWith(wildcard);
    }
}
