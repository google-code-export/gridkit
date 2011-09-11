package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.query.CqEvent;
import com.gemstone.gemfire.cache.query.CqListener;
import org.gridkit.gemfire.search.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IndexCqListener implements CqListener {
    private static Logger log = LoggerFactory.getLogger(IndexCqListener.class);

    private CountDownLatch preloadLatch;
    private DocumentFactory documentFactory;
    private IndexProcessor indexProcessor;

    public IndexCqListener(CountDownLatch preloadLatch,
                           DocumentFactory documentFactory,
                           IndexProcessor indexProcessor) {
        this.preloadLatch = preloadLatch;
        this.documentFactory = documentFactory;
        this.indexProcessor = indexProcessor;
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
            ObjectDocument objectDocument = documentFactory.createObjectDocument(
                cqEvent.getKey(), cqEvent.getNewValue()
            );

            if (operation.isCreate())
                indexProcessor.insert(objectDocument);
            else if (operation.isUpdate())
                indexProcessor.update(objectDocument);
        }
        else if (operation.isDestroy()) {
            indexProcessor.delete(Serialization.toString(cqEvent.getKey()));
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
