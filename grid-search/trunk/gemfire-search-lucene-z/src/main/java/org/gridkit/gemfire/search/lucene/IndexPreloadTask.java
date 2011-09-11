package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.query.CqResults;
import com.gemstone.gemfire.cache.query.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IndexPreloadTask implements Runnable {
    private final static Logger log = LoggerFactory.getLogger(IndexPreloadTask.class);

    private CqResults<Struct> cqResults;

    private IndexProcessor indexProcessor;

    private DocumentFactory documentFactory;

    private CountDownLatch preloadLatch;

    public IndexPreloadTask(CqResults<Struct> cqResults,
                            IndexProcessor indexProcessor,
                            DocumentFactory documentFactory,
                            CountDownLatch preloadLatch) {
        this.cqResults = cqResults;
        this.documentFactory = documentFactory;
        this.indexProcessor = indexProcessor;
        this.preloadLatch = preloadLatch;
    }

    @Override
    public void run() {
        for (Struct cqResult : cqResults) {
            Object key = cqResult.get("key");
            Object value = cqResult.get("value");

            ObjectDocument objectDocument = documentFactory.createObjectDocument(key, value);

            try {
                indexProcessor.insert(objectDocument);
            } catch (IOException e) {
                log.warn("IOException while processing initial result " + cqResult, e);
            }
        }

        preloadLatch.countDown();
    }
}
