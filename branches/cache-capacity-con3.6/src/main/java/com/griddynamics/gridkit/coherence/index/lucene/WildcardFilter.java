package com.griddynamics.gridkit.coherence.index.lucene;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.IndexAwareFilter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public class WildcardFilter implements IndexAwareFilter {

    private ValueExtractor extractor;
    private String wildcard;

    public WildcardFilter(String wildcard) {
        this(null, wildcard);
    }

    public WildcardFilter(ValueExtractor extractor, String wildcard) {
        this.extractor = new LuceneExtractor(extractor);
        this.wildcard = wildcard;
    }

    public int calculateEffectiveness(Map map, Set set) {
        LuceneMapIndex index = (LuceneMapIndex) map.get(extractor);
        return index == null ? Integer.MAX_VALUE : 1;
    }

    public Filter applyIndex(Map map, final Set set) {
        try {
            LuceneMapIndex index = (LuceneMapIndex) map.get(extractor);

            final IndexSearcher searcher = index.getIndexSearcher();
            final Collection keysToRetain = new ArrayList();

            searcher.search(
                    new WildcardQuery(new Term(LuceneMapIndex.VALUE, wildcard)),
                    new Collector() {
                        @Override
                        public void setScorer(Scorer scorer) throws IOException {
                        }

                        @Override
                        public void collect(int doc) throws IOException {
                            keysToRetain.add(
                                    ExternalizableHelper.fromByteArray(
                                        searcher.doc(doc).getField(LuceneMapIndex.KEY).getBinaryValue()));
                        }

                        @Override
                        public void setNextReader(IndexReader reader, int docBase) throws IOException {
                        }

                        @Override
                        public boolean acceptsDocsOutOfOrder() {
                            return false;
                        }
                    });

            set.retainAll(keysToRetain);

            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean evaluateEntry(Map.Entry entry) {
        return evaluate(entry);
    }

    public boolean evaluate(Object o) {
        String text = (String) extractor.extract(o);
        return text != null && text.startsWith(wildcard);
    }
}
