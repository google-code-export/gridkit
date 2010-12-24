package com.griddynamics.java.benchmarks.engine;

import com.griddynamics.java.benchmarks.model.Storage;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

/**
 * User: akondratyev
 */
public class StorageClient implements Runnable{

    private static Logger logger = LogManager.getLogger(StorageClient.class);

    private Storage storage;
    private int someKey = 2;

    public StorageClient(Storage storage) {
        if (storage == null)
            throw new NullPointerException("storage is null");
        this.storage = storage;
    }

    public void run() {
        long startTime;
        long endTime;
        while(true) {
            startTime = System.nanoTime();
            storage.getEntity(someKey);
            endTime = System.nanoTime();
            logger.debug("getting entity takes " + (endTime - startTime) + " nsec");
        }
    }
}
