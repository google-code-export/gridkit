package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.DistributedSystem;
import org.apache.lucene.search.Query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TODO implement discovery retries logic
public class GridIndexSearcher {
    private ConcurrentMap<String, DistributedMember> indexLocationMap = new ConcurrentHashMap<String, DistributedMember>();

    public <K> List<K> search(String regionFullPath, Query query) {
        String searchFunctionId = IndexSearchFunction.getId(regionFullPath);
        ResultCollector resultCollector = new IndexSearchResultCollector();

        Cache cache = CacheFactory.getAnyInstance();
        DistributedMember indexMemberId = findIndexMemberId(regionFullPath, cache);

        if (indexMemberId == null)
            throw new FunctionException("Failed to find index member");

        Execution execution = FunctionService.onMembers(cache.getDistributedSystem(), Collections.singleton(indexMemberId))
                                             .withArgs(query).withCollector(resultCollector);

        return (List<K>)execution.execute(searchFunctionId).getResult();
    }

    private DistributedMember findIndexMemberId(String regionFullPath, Cache cache) {
        if (!indexLocationMap.containsKey(regionFullPath)) {
            ResultCollector resultCollector = new IndexDiscoveryResultCollector();

            DistributedSystem distributedSystem = cache.getDistributedSystem();
            String indexHubId = cache.getRegion(regionFullPath).getAttributes().getGatewayHubId();

            Execution execution = FunctionService.onMembers(distributedSystem)
                                                 .withArgs(indexHubId)
                                                 .withCollector(resultCollector);

            DistributedMember indexMemberId = (DistributedMember)execution.execute(IndexDiscoveryFunction.Id).getResult();

            if (indexMemberId != null)
                indexLocationMap.put(regionFullPath, indexMemberId);
        }

        return indexLocationMap.get(regionFullPath);
    }
}
