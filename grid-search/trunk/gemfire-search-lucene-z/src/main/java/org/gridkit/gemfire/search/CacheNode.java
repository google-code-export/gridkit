package org.gridkit.gemfire.search;

import static org.gridkit.gemfire.search.DemoFactory.*;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionService;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.gridkit.gemfire.search.example.model.Author;
import org.gridkit.gemfire.search.lucene.LuceneGemfireSearcher;
import org.gridkit.gemfire.search.lucene.LuceneSearchFunction;

import java.io.IOException;
import java.util.Date;

public class CacheNode {
    public static String authors[] = {
        "Gavrila Derzhavin", "Nikolai Karamzin", "Alexander Pushkin",  "Mikhail Lermontov", "Nikolai Gogol",
        "Ivan Turgenev",     "Leo Tolstoy",      "Fyodor Dostoyevsky", "Anton Chekhov",     "Ivan Bunin"
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        Cache cache = createCache();

        Region<Integer, Author> authorRegion = createPartitionedRegion(
            cache, authorRegionName, authorHubName, true
        );

        Function searchFunction = LuceneSearchFunction.getStubSearchFunction(authorFunctionId);
        FunctionService.registerFunction(searchFunction);

        for (int i = 0; i < authors.length; ++i)
            authorRegion.put(i, new Author(i, authors[i], new Date()));

        LuceneGemfireSearcher<Integer, Author> searcher = new LuceneGemfireSearcher<Integer, Author>(
            authorRegion, cache.getDistributedSystem(), authorFunctionId
        );

        Thread.sleep(1000);

        Query query = new TermQuery(new Term("name", "pushkin"));
        System.out.println(searcher.search(query));
    }
}
