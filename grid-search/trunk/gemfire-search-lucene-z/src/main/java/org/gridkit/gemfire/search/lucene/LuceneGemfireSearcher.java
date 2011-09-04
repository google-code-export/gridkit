package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedSystem;
import org.apache.lucene.search.Query;

import java.util.Map;

public class LuceneGemfireSearcher<K, V> {
    private Region<K, V> region;

    private DistributedSystem distributedSystem;

    private String searchFunctionId;

    public LuceneGemfireSearcher(Region<K, V> region,
                                 DistributedSystem distributedSystem,
                                 String searchFunctionId) {
        this.region = region;
        this.distributedSystem = distributedSystem;
        this.searchFunctionId = searchFunctionId;
    }

    public Map<K, V> search(Query query) {
        ResultCollector luceneResultCollector = new LuceneResultCollector(region);

        Execution execution = FunctionService.onMembers(distributedSystem)
                                             .withArgs(query)
                                             .withCollector(luceneResultCollector);

        ResultCollector finalResultCollector = execution.execute(searchFunctionId);

        return (Map<K, V>)finalResultCollector.getResult();
    }
}
