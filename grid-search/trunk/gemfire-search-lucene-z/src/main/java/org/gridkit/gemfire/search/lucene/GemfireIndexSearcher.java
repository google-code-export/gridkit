package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.CacheFactory;
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
//TODO implement index node balancing
public class GemfireIndexSearcher {
    private final DistributedSystem distributedSystem = CacheFactory.getAnyInstance().getDistributedSystem();

    private final ConcurrentMap<String, List<DistributedMember>> indexLocationsMap =
        new ConcurrentHashMap<String, List<DistributedMember>>();

    public <K> List<K> search(String regionFullPath, Query query) {
        ResultCollector resultCollector = new IndexSearchResultCollector();

        DistributedMember indexMemberId = selectIndexMemberId(regionFullPath);

        if (indexMemberId == null)
            throw new FunctionException("Failed to find index member");

        Execution execution = FunctionService.onMembers(distributedSystem, Collections.singleton(indexMemberId))
                                             .withArgs(query).withCollector(resultCollector);

        return (List<K>)execution.execute(IndexSearchFunction.Id).getResult();
    }

    private void findIndexMemberIds(String regionFullPath) {
        ResultCollector resultCollector = new IndexDiscoveryResultCollector();

        Execution execution = FunctionService.onMembers(distributedSystem)
                                             .withArgs(regionFullPath)
                                             .withCollector(resultCollector);

        List<DistributedMember> indexMemberIds =
            (List<DistributedMember>)execution.execute(IndexDiscoveryFunction.Id).getResult();

        if (indexMemberIds.size() != 0)
            indexLocationsMap.put(regionFullPath, indexMemberIds);
    }

    private DistributedMember selectIndexMemberId(String regionFullPath) {
        if (!indexLocationsMap.containsKey(regionFullPath))
            findIndexMemberIds(regionFullPath);

        List<DistributedMember> indexLocations = indexLocationsMap.get(regionFullPath);

        if (indexLocations != null)
            return indexLocations.get(0);
        else
            return null;
    }
}
