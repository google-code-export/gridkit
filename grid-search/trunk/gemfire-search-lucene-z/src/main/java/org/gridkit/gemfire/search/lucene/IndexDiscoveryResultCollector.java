package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class IndexDiscoveryResultCollector implements ResultCollector<Boolean, Serializable> {
    private DistributedMember hubMemberId = null;

    @Override
    public void addResult(DistributedMember memberID, Boolean hubExists) {
        if (hubExists)
            hubMemberId = memberID;
    }

    @Override
    public Serializable getResult() throws FunctionException {
        return (Serializable)hubMemberId;
    }

    @Override
    public Serializable getResult(long timeout, TimeUnit unit) throws FunctionException, InterruptedException {
        return (Serializable)hubMemberId;
    }

    @Override
    public void endResults() {}

    @Override
    public void clearResults() {}
}
