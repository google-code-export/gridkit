package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IndexDiscoveryResultCollector implements ResultCollector<Boolean, Serializable> {
    private List<DistributedMember> indexMemberIds = new ArrayList<DistributedMember>();

    @Override
    public void addResult(DistributedMember memberID, Boolean indexProcessorExists) {
        if (indexProcessorExists)
            indexMemberIds.add(memberID);
    }

    @Override
    public Serializable getResult() throws FunctionException {
        return (Serializable)indexMemberIds;
    }

    @Override
    public Serializable getResult(long timeout, TimeUnit unit) throws FunctionException, InterruptedException {
        return (Serializable)indexMemberIds;
    }

    @Override
    public void endResults() {}

    @Override
    public void clearResults() {}
}
