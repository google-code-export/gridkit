package org.gridkit.search.gemfire;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.search.IndexSearcher;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class LuceneIndexManager {

	public static final String DOCUMENT_KEY = "#dockey#";
	
	private static LuceneIndexManager INSTANCE = new LuceneIndexManager();
	
	public static LuceneIndexManager getInstance() {
		return INSTANCE;
	}
	
	private LuceneIndexManager() {
	}
	
	private Map<String, IndexSearcher> indexes = new ConcurrentHashMap<String, IndexSearcher>();
	
	public IndexSearcher getSearcherForRegion(String regionName){
		return indexes.get(regionName);
	}
	
	public void setSearcherForRegion(String regionName, IndexSearcher searcher) {
		indexes.put(regionName, searcher);
	}
}
