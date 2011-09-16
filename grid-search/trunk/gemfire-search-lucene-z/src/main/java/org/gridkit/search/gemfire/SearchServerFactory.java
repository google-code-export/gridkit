package org.gridkit.search.gemfire;

import com.gemstone.gemfire.cache.query.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.spi.InternalCompass;
import org.gridkit.search.lucene.IndexableFactory;
import org.gridkit.search.lucene.SearchEngine;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class SearchServerFactory {
    public static final String searchServerRole = String.format("Role(%s)", SearchServerFactory.class.getName());

    private SearchServerConfig searchServerConfig;

    private InternalCompass compass;
    private IndexableFactory indexableFactory;
    private SearchEngineRegistry searchEngineRegistry;

    private IndexDiscoveryFunction discoveryFunction;
    private IndexSearchFunction searchFunction;

    public SearchServerFactory(SearchServerConfig searchServerConfig, InternalCompass compass) {
        this.searchServerConfig = searchServerConfig;

        this.compass = compass;
        this.indexableFactory = new CompassIndexableFactory(compass, searchServerConfig.getKeyFieldName());
        this.searchEngineRegistry = new ConcurrentSearchEngineRegistry();

        this.discoveryFunction = new IndexDiscoveryFunction(searchEngineRegistry);
        this.searchFunction = new IndexSearchFunction(searchServerConfig.getKeyFieldName(), searchEngineRegistry);
    }

    public CqQuery createRegionIndex(String regionFullPath, QueryService queryService, ExecutorService executorService) throws IOException, CqExistsException, CqException, RegionNotFoundException {
        CountDownLatch preloadLatch = new CountDownLatch(1);

        IndexWriterConfig indexWriterConfig = createIndexWriterConfig();
        Directory directory = createMemoryDirectory();

        SearchEngine indexProcessor = createSearchEngine(
            directory, indexWriterConfig, searchServerConfig
        );

        IndexCqListener indexCqListener = new IndexCqListener(
            preloadLatch, indexableFactory, indexProcessor
        );

        CqAttributesFactory cqAttrFact = new CqAttributesFactory();
        cqAttrFact.addCqListener(indexCqListener);
        CqAttributes cqAttrs = cqAttrFact.create();

        String cqName = getCqName(regionFullPath);
        String cqQueryStr = String.format("SELECT * FROM %s", regionFullPath);

        CqQuery cqQuery = queryService.newCq(cqName, cqQueryStr, cqAttrs);

        CqResults<Struct> cqResults = cqQuery.executeWithInitialResults();

        Runnable indexPreloadTask = new IndexPreloadTask (
            cqResults, preloadLatch, indexableFactory, indexProcessor
        );

        searchEngineRegistry.registerSearchEngine(regionFullPath, indexProcessor);

        executorService.submit(indexPreloadTask);

        return cqQuery;
    }

    public void close() {
        searchEngineRegistry.close();
    }

    private SearchEngine createSearchEngine(Directory directory,
                                            IndexWriterConfig indexWriterConfig,
                                            SearchServerConfig searchServerConfig) throws IOException {
        return new LuceneNRTSearchEngine(
            directory, indexWriterConfig,
            searchServerConfig.getChangesBeforeCommit()
        );
    }

    public IndexDiscoveryFunction getDiscoveryFunction() {
        return discoveryFunction;
    }

    public IndexSearchFunction getSearchFunction() {
        return searchFunction;
    }

    public static String getCqName(String regionFullPath) {
        return String.format("FullTextIndex(%s)", regionFullPath);
    }

    private Directory createMemoryDirectory() throws IOException {
        Directory directory = new RAMDirectory();

        IndexWriterConfig indexWriterConfig = createIndexWriterConfig();

        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        indexWriter.commit(); indexWriter.close();

        return directory;
    }

    private IndexWriterConfig createIndexWriterConfig() {
        return new IndexWriterConfig(Version.LUCENE_33, getDefaultAnalyzer());
    }

    private Analyzer getDefaultAnalyzer() {
        LuceneSearchEngineFactory searchEngineFactory =
            (LuceneSearchEngineFactory)compass.getSearchEngineFactory();

        return searchEngineFactory.getAnalyzerManager().getAnalyzer(
            LuceneEnvironment.Analyzer.DEFAULT_GROUP
        );
    }
}
