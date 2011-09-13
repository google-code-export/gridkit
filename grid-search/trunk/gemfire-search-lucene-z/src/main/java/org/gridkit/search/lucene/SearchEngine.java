package org.gridkit.search.lucene;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;

public interface SearchEngine {
	public void insert(Indexable indexable) throws IOException;

	public void update(Indexable indexable) throws IOException;

	public void delete(Term term) throws IOException;
	
	public IndexSearcher acquireSearcher() throws IOException;
	public void releaseSearcher(IndexSearcher searcher);

    public void close();
}
