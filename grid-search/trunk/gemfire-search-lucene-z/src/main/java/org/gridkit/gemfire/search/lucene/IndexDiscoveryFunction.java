package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.util.GatewayHub;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IndexDiscoveryFunction implements Function, DataSerializable {
    public static final Function Instance = new IndexDiscoveryFunction();

    public static final String Id = IndexDiscoveryFunction.class.getName();

    @Override
    public void execute(FunctionContext functionContext) {
        String hubId = (String)functionContext.getArguments();

        Cache cache = CacheFactory.getAnyInstance();
        boolean hubExists = false;

        for (GatewayHub hub : cache.getGatewayHubs()) {
            if (hub.getId().equals(hubId) && hub.isPrimary())
                hubExists = true;
        }

        functionContext.getResultSender().lastResult(hubExists);
    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }

    @Override
    public void toData(DataOutput output) throws IOException {}

    @Override
    public void fromData(DataInput paramDataInput) throws IOException, ClassNotFoundException {}
}
