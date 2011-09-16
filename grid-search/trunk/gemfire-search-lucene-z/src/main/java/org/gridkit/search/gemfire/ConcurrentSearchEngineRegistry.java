package org.gridkit.search.gemfire;

import org.gridkit.search.lucene.SearchEngine;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConcurrentSearchEngineRegistry implements SearchEngineRegistry {
    private final ConcurrentMap<String, SearchEngine> indexProcessorMap =
        new ConcurrentHashMap<String, SearchEngine>();

    @Override
    public void registerSearchEngine(String searchEngineName, SearchEngine searchEngine) {
        indexProcessorMap.put(searchEngineName, searchEngine);
    }

    @Override
    public void unregisterSearchEngine(String searchEngineName) {
        indexProcessorMap.remove(searchEngineName);
    }

    @Override
    public SearchEngine getSearchEngine(String searchEngineName) {
        return indexProcessorMap.get(searchEngineName);
    }

    @Override
    public boolean hasSearchEngine(String searchEngineName) {
        return indexProcessorMap.containsKey(searchEngineName);
    }

    @Override
    public void close() {

    }
}
