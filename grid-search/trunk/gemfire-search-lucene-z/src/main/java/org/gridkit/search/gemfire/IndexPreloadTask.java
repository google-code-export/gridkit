package org.gridkit.search.gemfire;

import com.gemstone.gemfire.cache.query.CqResults;
import com.gemstone.gemfire.cache.query.Struct;
import org.gridkit.search.lucene.Indexable;
import org.gridkit.search.lucene.IndexableFactory;
import org.gridkit.search.lucene.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IndexPreloadTask implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(IndexPreloadTask.class);

    private CqResults<Struct> cqResults;

    private SearchEngine searchEngine;

    private IndexableFactory indexableFactory;

    private CountDownLatch preloadLatch;

    public IndexPreloadTask(CqResults<Struct> cqResults,
                            CountDownLatch preloadLatch,
                            IndexableFactory indexableFactory,
                            SearchEngine searchEngine) {
        this.cqResults = cqResults;
        this.indexableFactory = indexableFactory;
        this.searchEngine = searchEngine;
        this.preloadLatch = preloadLatch;
    }

    @Override
    public void run() {
        for (Struct cqResult : cqResults) {
            Object key = cqResult.get("key");
            Object value = cqResult.get("value");

            try {
                Indexable indexable = indexableFactory.createIndexable(key, value);
                searchEngine.insert(indexable);
            } catch (IOException e) {
                log.warn("IOException while processing initial result " + cqResult, e);
            }
        }

        preloadLatch.countDown();
    }
}
