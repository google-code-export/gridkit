package org.gridkit.search.gemfire;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.search.Query;

import com.gemstone.gemfire.distributed.DistributedMember;

public class SearchService {

	private static final SearchService INSTANCE = new SearchService();
	
	public static SearchService getInstance() {
		return INSTANCE;
	}
	
	private Map<String, DistributedMember> indexLocations = new ConcurrentHashMap<String, DistributedMember>();
	
	private SearchService() {		
	}
	
	@SuppressWarnings("unchecked")
	public <K> Set<K> keySet(String regionName, Query searchQuery) {
		int tryes = 3;
		while(tryes > 0) {
			DistributedMember member = getIndexNode(regionName);
			if (member == null) {
				throw new IllegalStateException("No index for '" + regionName + "' is found in distributed system");
			}
			else {
				try {
					List<Object> result = LuceneKeySetQueryTask.execute(member, regionName, searchQuery);
					if (result.size() == 1 && result.get(0) == IndexError.NO_INDEX) {
						indexLocations.remove(regionName);
						--tryes;
						continue;
					}
					Set<K> keySet = new HashSet<K>();
					for(Object key: result) {
						// TODO unelegant
						if (key instanceof String) {
							keySet.add((K) KeyCodec.stringToObject((String) key));
						}						
					}
					return keySet;
				}
				catch (Exception e) {
					indexLocations.remove(regionName);
					--tryes;
					continue;
				}
			}
		}
		throw new IllegalStateException("No index availble for '" + regionName + "'");
	}

	private DistributedMember getIndexNode(String regionName) {
		DistributedMember node = indexLocations.get(regionName);
		if (node == null) {
			try {
				node = LuceneIndexDiscoveryTask.execute(regionName);
				if (node != null) {
					indexLocations.put(regionName, node);
				}
			} catch (InterruptedException e) {
				return null;
			}
		}
		
		return node;
	}
}
