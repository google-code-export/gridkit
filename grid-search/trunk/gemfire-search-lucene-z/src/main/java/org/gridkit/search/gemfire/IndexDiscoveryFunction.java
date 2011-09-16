package org.gridkit.search.gemfire;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexDiscoveryFunction implements Function {
    private static Logger log = LoggerFactory.getLogger(IndexDiscoveryFunction.class);

    public static final String Id = IndexDiscoveryFunction.class.getName();

    private final SearchEngineRegistry indexProcessorRegistry;

    public IndexDiscoveryFunction(SearchEngineRegistry indexProcessorRegistry) {
        this.indexProcessorRegistry = indexProcessorRegistry;
    }

    @Override
    public void execute(FunctionContext functionContext) {
        log.info("Starting execution of " + IndexDiscoveryFunction.class.getName());

        String indexProcessorName = (String)functionContext.getArguments();

        functionContext.getResultSender().lastResult(
            indexProcessorRegistry.hasSearchEngine(indexProcessorName)
        );
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

    public static Function getIndexDiscoveryFunctionStub() {
        return new Function() {
            @Override
            public boolean hasResult() { return true; }

            @Override
            public void execute(FunctionContext functionContext) {
                log.info("Starting execution of " + IndexDiscoveryFunction.class.getName() + " stub");
                functionContext.getResultSender().lastResult(false);
            }

            @Override
            public String getId() {
                return Id;
            }

            @Override
            public boolean optimizeForWrite() { return false; }

            @Override
            public boolean isHA() { return false; }
        };
    }
}
