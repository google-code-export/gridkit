package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

public interface IndexProcessor {
    public void insert(ObjectDocument objectDocument) throws IOException;
    public void update(ObjectDocument objectDocument) throws IOException;

    public void delete(String objectKey) throws IOException;

    public IndexSearcher getIndexSearcher() throws IOException;
    public void releaseIndexSearcher(IndexSearcher indexSearcher);
}
