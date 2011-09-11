package org.gridkit.gemfire.search.lucene;

public interface DocumentFactory {
    ObjectDocument createObjectDocument(Object key, Object value);
}
