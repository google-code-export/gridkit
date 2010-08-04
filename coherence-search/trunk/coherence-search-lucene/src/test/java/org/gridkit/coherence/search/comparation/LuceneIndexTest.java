package org.gridkit.coherence.search.comparation;

import com.tangosol.util.Filter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.gridkit.coherence.search.lucene.LuceneDocumentExtractor;
import org.gridkit.coherence.search.lucene.LuceneSearchFactory;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public class LuceneIndexTest extends IndexComparisonTestBase {

    private LuceneSearchFactory factory;

    @Override
    protected void setUp() {
        LuceneDocumentExtractor extractor = new LuceneDocumentExtractor();

        for (int i = 0; i < N; i++) {
            extractor.addText("stringField" + i, stringFieldExtractors[i]);
            extractor.addText("intField" + i, intFieldExtractors[i]);
        }

        factory = new LuceneSearchFactory(extractor);
        factory.getEngineConfig().setIndexUpdateQueueSizeLimit(0);
        factory.getEngineConfig().setIndexUpdateDelay(60000);

        factory.createIndex(cache);
    }

    @Override
    protected Set entrySet() {

        BooleanQuery query = new BooleanQuery();

        for (int i = 0; i < N; i++) {
            query.add(new TermQuery(new Term("stringField" + i, String.valueOf(i))), BooleanClause.Occur.MUST);
            query.add(new TermQuery(new Term("intField" + i, String.valueOf(i + N))), BooleanClause.Occur.MUST);
        }

        Filter filter = factory.createFilter(query);
        
        return cache.entrySet(filter);
    }
}
