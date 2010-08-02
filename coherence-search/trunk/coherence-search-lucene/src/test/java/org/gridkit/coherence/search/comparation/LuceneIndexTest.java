package org.gridkit.coherence.search.comparation;

import com.tangosol.util.Filter;
import com.tangosol.util.extractor.ReflectionExtractor;
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

public class LuceneIndexTest extends ComparationIndexTestBase {

    private LuceneSearchFactory factory;

    @Override
    protected void setUp() {
        LuceneDocumentExtractor extractor = new LuceneDocumentExtractor();
        extractor.addText("stringField", new ReflectionExtractor("getStringField"));
        extractor.addText("intField", new ReflectionExtractor("getIntField"));

        factory = new LuceneSearchFactory(extractor);
        factory.getEngineConfig().setIndexUpdateQueueSizeLimit(100000);

        factory.createIndex(cache);
    }

    @Override
    protected Set entrySet() {

        BooleanQuery query = new BooleanQuery();

        query.add(new TermQuery(new Term("stringField", "1")), BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term("intField", "1")), BooleanClause.Occur.MUST);

        Filter filter = factory.createFilter(query);
        
        return cache.entrySet(filter);
    }
}
