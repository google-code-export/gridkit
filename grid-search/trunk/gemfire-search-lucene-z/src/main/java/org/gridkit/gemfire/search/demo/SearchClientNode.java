package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.FieldMaskingSpanQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.Version;
import org.gridkit.gemfire.search.demo.model.Commitment;
import org.gridkit.gemfire.search.demo.model.Fts;
import org.gridkit.gemfire.search.demo.model.JaxbFactory;
import org.gridkit.search.gemfire.GemfireIndexSearcher;
import org.gridkit.search.gemfire.IndexDiscoveryFunction;
import org.gridkit.search.gemfire.IndexSearchFunction;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;
import static org.gridkit.gemfire.search.demo.DemoFactory.createServerRegion;

public class SearchClientNode implements Callable<Void> {
    public static String authors[] = {
        "Gavrila Derzhavin", "Nikolai Karamzin", "Alexander Pushkin",  "Mikhail Lermontov", "Nikolai Gogol",
        "Ivan Turgenev",     "Leo Tolstoy",      "Fyodor Dostoyevsky", "Anton Chekhov",     "Ivan Bunin"
    };

    private CountDownLatch searchServerLatch;

    public SearchClientNode(CountDownLatch searchServerLatch) {
        this.searchServerLatch = searchServerLatch;
    }

    @Override
    public Void call() throws Exception {
        searchServerLatch.await();

        Cache cache = createServerCache();
        Region authorRegion = createServerRegion(cache, DemoFactory.authorRegionName, false);

        /*
        for (int i = 0; i < authors.length; ++i) {
            authorRegion.put(i, new Author(i, authors[i], new Date()));
            System.out.println("+++++++++++++++++");
        }
        */

        Unmarshaller unmarshaller = JaxbFactory.createUnmarshaller();
        Marshaller marshaller = JaxbFactory.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

       // Fts fts = (Fts)unmarshaller.unmarshal(ClassLoader.getSystemClassLoader().getResourceAsStream("data/fts.sample.xml"));
        Fts fts = (Fts)unmarshaller.unmarshal(new File("/home/art/Downloads/export_2009_en.xml"));


        for (Commitment commitment : fts.getCommitments())
            authorRegion.put(commitment.getPositionKey(), commitment);

        FunctionService.registerFunction(IndexDiscoveryFunction.getIndexDiscoveryFunctionStub());
        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub());

        Thread.sleep(1000);

        //GemfireIndexSearcher searcher = new GemfireIndexSearcher(cache.getDistributedSystem());
        //Query query = new TermQuery(new Term("name", "pushkin"));
        //System.out.println("#############" + searcher.search(authorRegion.getFullPath(), query));

        QueryParser parser = new QueryParser(Version.LUCENE_33, "notUsed", new StopAnalyzer(Version.LUCENE_33));

        SpanQuery[] spans = new SpanQuery[] {
            new SpanTermQuery(new Term("city", "gent")),
            new FieldMaskingSpanQuery(new SpanTermQuery(new Term("country", "belgium")), "city")
        };
        Query query = new SpanNearQuery(spans, -1, false);

        //Query query = parser.parse("+city:gent +country:belgium");

        //Query query = parser.parse("+city:STUTTGART +country:Germany");

        GemfireIndexSearcher searcher = new GemfireIndexSearcher(cache.getDistributedSystem());
        searcher.search(authorRegion.getFullPath(), query);
        for (Object key : searcher.search(authorRegion.getFullPath(), query)) {
            marshaller.marshal(authorRegion.get(key), System.err);
        }

        return null;
    }
}
