package org.gridkit.gemfire.search.demo;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gridkit.gemfire.search.demo.model.Author;
import org.gridkit.gemfire.search.lucene.IndexDiscoveryFunction;
import org.gridkit.gemfire.search.lucene.GridIndexSearcher;
import org.gridkit.gemfire.search.lucene.IndexSearchFunction;

import java.util.Date;
import java.util.concurrent.Callable;

public class CacheNode implements Callable<Void> {
    public static String authors[] = {
        "Gavrila Derzhavin", "Nikolai Karamzin", "Alexander Pushkin",  "Mikhail Lermontov", "Nikolai Gogol",
        "Ivan Turgenev",     "Leo Tolstoy",      "Fyodor Dostoyevsky", "Anton Chekhov",     "Ivan Bunin"
    };

    @Override
    public Void call() throws Exception {
        Thread.sleep(5000);

        Cache cache = DemoFactory.createCache();

        Region<Integer, Author> authorRegion = DemoFactory.createPartitionedRegion(
                cache, DemoFactory.authorRegionName, DemoFactory.authorHubName, true
        );

        for (int i = 0; i < authors.length; ++i)
            authorRegion.put(i, new Author(i, authors[i], new Date()));

        FunctionService.registerFunction(IndexSearchFunction.getIndexSearchFunctionStub(authorRegion.getFullPath()));
        FunctionService.registerFunction(IndexDiscoveryFunction.Instance);

        Thread.sleep(1000);

        GridIndexSearcher searcher = new GridIndexSearcher();
        Query query = new TermQuery(new Term("name", "pushkin"));
        System.out.println("#############" + searcher.search(authorRegion.getFullPath(), query));

        return null;
    }
}
