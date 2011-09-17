package org.gridkit.search.gemfire.benchmark;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;

import java.io.Serializable;

public final class GcFunction implements Function, Serializable {
    public static final Function Instance = new GcFunction();

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public void execute(FunctionContext functionContext) {
        System.gc();
        functionContext.getResultSender().lastResult(true);
    }

    @Override
    public String getId() {
        return GcFunction.class.getName();
    }

    @Override
    public boolean optimizeForWrite() {
        return false;
    }

    @Override
    public boolean isHA() {
        return false;
    }
}
