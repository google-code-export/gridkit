package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.query.CqEvent;
import com.gemstone.gemfire.cache.query.CqListener;
import org.gridkit.gemfire.search.util.Serialization;
import org.gridkit.search.lucene.Indexable;
import org.gridkit.search.lucene.IndexableFactory;
import org.gridkit.search.lucene.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IndexCqListener implements CqListener {
    private static Logger log = LoggerFactory.getLogger(IndexCqListener.class);

    private CountDownLatch preloadLatch;
    private IndexableFactory indexableFactory;
    private SearchEngine searchEngine;

    public IndexCqListener(CountDownLatch preloadLatch,
                           IndexableFactory indexableFactory,
                           SearchEngine searchEngine) {
        this.preloadLatch = preloadLatch;
        this.indexableFactory = indexableFactory;
        this.searchEngine = searchEngine;
    }

    @Override
    public void onEvent(CqEvent cqEvent) {
        try {
            processCqEvent(cqEvent);
        } catch (IOException e) {
            log.warn("IOException while processing CQ event " + cqEvent, e);
        }
    }

    private void processCqEvent(CqEvent cqEvent) throws IOException {
        if (preloadLatch.getCount() > 0)
            waitForPreload();

        Operation operation = cqEvent.getQueryOperation();

        if (operation.isCreate() || operation.isUpdate()) {
            Indexable indexable = indexableFactory.createIndexable(
                cqEvent.getKey(), cqEvent.getNewValue()
            );

            if (operation.isCreate())
                searchEngine.insert(indexable);
            else if (operation.isUpdate())
                searchEngine.update(indexable);
        }
        else if (operation.isDestroy()) {
            searchEngine.delete(indexableFactory.createKeyTerm(cqEvent.getKey()));
        }
    }

    @Override
    public void onError(CqEvent cqEvent) {
        log.warn("CQ error received " + cqEvent);
    }

    private void waitForPreload() {
        try {
            preloadLatch.await();
        } catch (InterruptedException e) {
            log.warn("InterruptedException while waiting preload future", e);
        }
    }

    @Override
    public void close() {}
}
