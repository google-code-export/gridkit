package org.gridkit.gemfire.search.lucene;

public interface IndexProcessorRegistry {
    void registerIndexProcessor(String indexProcessorName, IndexProcessor indexProcessor);
    void unregisterIndexProcessor(String indexProcessorName);

    IndexProcessor getIndexProcessor(String indexProcessorName);
    boolean hasIndexProcessor(String indexProcessorName);
}
