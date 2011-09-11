package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gridkit.gemfire.search.demo.model.Author;
import org.gridkit.gemfire.search.lucene.IndexDiscoveryFunction;
import org.gridkit.gemfire.search.lucene.GemfireIndexSearcher;
import org.gridkit.gemfire.search.lucene.IndexSearchFunction;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static org.gridkit.gemfire.search.demo.DemoFactory.authorRegionName;
import static org.gridkit.gemfire.search.demo.DemoFactory.createClientCache;
import static org.gridkit.gemfire.search.demo.DemoFactory.createClientRegion;

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

        ClientCache cache = createClientCache();
        Region<Integer, Author> authorRegion = createClientRegion(cache, authorRegionName);

        for (int i = 0; i < authors.length; ++i)
            authorRegion.put(i, new Author(i, authors[i], new Date()));

        FunctionService.registerFunction(IndexDiscoveryFunction.getIndexDiscoveryFunctionStub());
        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub());

        GemfireIndexSearcher searcher = new GemfireIndexSearcher();
        Query query = new TermQuery(new Term("name", "pushkin"));
        System.out.println("#############" + searcher.search(authorRegion.getFullPath(), query));

        return null;
    }
}
