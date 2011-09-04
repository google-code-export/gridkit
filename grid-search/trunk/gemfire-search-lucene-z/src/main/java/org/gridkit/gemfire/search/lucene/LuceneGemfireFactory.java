package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalCompass;
import org.gridkit.gemfire.search.compass.marshall.GridkitMarshallingStrategy;

import java.io.IOException;

public class LuceneGemfireFactory {
    private InternalCompass compass;

    private Directory directory;
    private IndexWriterConfig indexWriterConfig;

    private LuceneGemfireConfig luceneGemfireConfig;

    private String compassAllProperty;
    private MarshallingStrategy marshallingStrategy;

    private LuceneIndexProcessor indexProcessor;
    private LuceneIndexGatewayListener gatewayListener;
    private LuceneSearchFunction searchFunction;

    public LuceneGemfireFactory(InternalCompass compass,
                                Directory directory,
                                IndexWriterConfig indexWriterConfig,
                                LuceneGemfireConfig luceneGemfireConfig) throws IOException {
        this.compass = compass;
        this.directory = directory;
        this.indexWriterConfig = indexWriterConfig;
        this.luceneGemfireConfig = luceneGemfireConfig;

        createCompassStructures();

        createIndexProcessor();
        createGatewayListener();
        createSearchFunction();
    }

    private void createCompassStructures() {
        LuceneSearchEngineFactory searchEngineFactory = (LuceneSearchEngineFactory)compass.getSearchEngineFactory();

        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(compass.getSettings());
        SearchEngine searchEngine = searchEngineFactory.openSearchEngine(runtimeSettings);

        this.marshallingStrategy = new GridkitMarshallingStrategy(
            compass.getMapping(), searchEngine, compass.getConverterLookup(),
            compass.getResourceFactory(), compass.getPropertyNamingStrategy()
        );

        this.compassAllProperty = searchEngineFactory.getLuceneSettings().getAllProperty();
    }

    private void createIndexProcessor() throws IOException {
        indexProcessor = new LuceneIndexProcessor(
            directory, indexWriterConfig,
            luceneGemfireConfig.getChangesBeforeCommit(),
            luceneGemfireConfig.getKeyFieldName()
        );
    }

    private void createGatewayListener() {
        gatewayListener = new LuceneIndexGatewayListener(
                marshallingStrategy, indexProcessor,
                luceneGemfireConfig.getKeyFieldName(),
                compassAllProperty
        );
    }

    private void createSearchFunction() {
        searchFunction = new LuceneSearchFunction(
            indexProcessor,
            luceneGemfireConfig.getSearchFunctionId(),
            luceneGemfireConfig.getKeyFieldName()
        );
    }

    public LuceneIndexGatewayListener getGatewayListener() {
        return gatewayListener;
    }

    public LuceneSearchFunction getSearchFunction() {
        return searchFunction;
    }

    public static Analyzer getDefaultAnalyzer(InternalCompass compass) {
        LuceneSearchEngineFactory searchEngineFactory =
            (LuceneSearchEngineFactory)compass.getSearchEngineFactory();

        return searchEngineFactory.getAnalyzerManager().getAnalyzer(
            LuceneEnvironment.Analyzer.DEFAULT_GROUP
        );
    }
}
