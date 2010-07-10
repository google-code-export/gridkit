package com.griddynamics.gridkit.coherence.index.lucene;

import com.tangosol.util.Binary;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.IndexAwareFilter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.*;

/**
 * @author Alexander Solovyov
 */

public class PrefixFilter implements IndexAwareFilter {

    private ValueExtractor extractor;
    private String prefix;

    public PrefixFilter(String prefix) {
        this(null, prefix);
    }

    public PrefixFilter(ValueExtractor extractor, String prefix) {
        this.extractor = new LuceneExtractor(extractor);
        this.prefix = prefix;
    }

    public int calculateEffectiveness(Map map, Set set) {
        LuceneMapIndex index = (LuceneMapIndex) map.get(extractor);
        return index == null ? Integer.MAX_VALUE : 1;
    }

    public Filter applyIndex(Map map, final Set set) {
        try {
            LuceneMapIndex index = (LuceneMapIndex) map.get(extractor);
            final IndexSearcher searcher = new IndexSearcher(index.getDirectory());
            Query query = new WildcardQuery(new Term("value", prefix + "*"));

            final Collection<Binary> keysToRetain = new ArrayList<Binary>();

            searcher.search(
                    query,
                    new Collector() {
                        @Override
                        public void setScorer(Scorer scorer) throws IOException {
                        }

                        @Override
                        public void collect(int doc) throws IOException {
                            Binary binary = new Binary(searcher.doc(doc).getField("key").getBinaryValue());
                            keysToRetain.add(binary);
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
        return text != null && text.startsWith(prefix);
    }
}
