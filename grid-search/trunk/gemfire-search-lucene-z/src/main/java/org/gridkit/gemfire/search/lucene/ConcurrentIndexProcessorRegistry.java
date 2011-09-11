package org.gridkit.gemfire.search.lucene;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConcurrentIndexProcessorRegistry implements IndexProcessorRegistry {
    private final ConcurrentMap<String, IndexProcessor> indexProcessorMap =
        new ConcurrentHashMap<String, IndexProcessor>();

    @Override
    public void registerIndexProcessor(String indexProcessorName, IndexProcessor indexProcessor) {
        indexProcessorMap.put(indexProcessorName, indexProcessor);
    }

    @Override
    public void unregisterIndexProcessor(String indexProcessorName) {
        indexProcessorMap.remove(indexProcessorName);
    }

    @Override
    public IndexProcessor getIndexProcessor(String indexProcessorName) {
        return indexProcessorMap.get(indexProcessorName);
    }

    @Override
    public boolean hasIndexProcessor(String indexProcessorName) {
        return indexProcessorMap.containsKey(indexProcessorName);
    }
}
