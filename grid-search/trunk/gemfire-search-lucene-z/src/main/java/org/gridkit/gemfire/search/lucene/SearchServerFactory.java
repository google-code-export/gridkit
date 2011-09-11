package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Region;
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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class SearchServerFactory {
    private SearchServerConfig searchServerConfig;

    private InternalCompass compass;
    private DocumentFactory documentFactory;
    private IndexProcessorRegistry indexProcessorRegistry;

    private IndexDiscoveryFunction discoveryFunction;
    private IndexSearchFunction searchFunction;

    public SearchServerFactory(SearchServerConfig searchServerConfig, InternalCompass compass) {
        this.searchServerConfig = searchServerConfig;

        this.compass = compass;
        this.documentFactory = new CompassDocumentFactory(compass, searchServerConfig.getKeyFieldName());
        this.indexProcessorRegistry = new ConcurrentIndexProcessorRegistry();

        this.discoveryFunction = new IndexDiscoveryFunction(indexProcessorRegistry);
        this.searchFunction = new IndexSearchFunction(searchServerConfig.getKeyFieldName(), indexProcessorRegistry);
    }

    public CqQuery createRegionIndex(Region<?, ?> region, ExecutorService executorService) throws IOException, CqExistsException, CqException, RegionNotFoundException {
        CountDownLatch preloadLatch = new CountDownLatch(1);

        IndexWriterConfig indexWriterConfig = createIndexWriterConfig();
        Directory directory = createMemoryDirectory();

        IndexProcessor indexProcessor = createIndexProcessor(
            directory, indexWriterConfig, searchServerConfig
        );

        IndexCqListener indexCqListener = new IndexCqListener(
            preloadLatch, documentFactory, indexProcessor
        );

        QueryService queryService = region.getRegionService().getQueryService();

        CqAttributesFactory cqAttrFact = new CqAttributesFactory();
        cqAttrFact.addCqListener(indexCqListener);
        CqAttributes cqAttrs = cqAttrFact.create();

        String cqName = getCqName(region.getFullPath());
        String cqQueryStr = String.format("SELECT * FROM %s", region.getFullPath());

        CqQuery cqQuery = queryService.newCq(cqName, cqQueryStr, cqAttrs);

        CqResults<Struct> cqResults = cqQuery.executeWithInitialResults();

        Runnable indexPreloadTask = new IndexPreloadTask (
            cqResults, indexProcessor, documentFactory, preloadLatch
        );

        executorService.submit(indexPreloadTask);

        return cqQuery;
    }

    private IndexProcessor createIndexProcessor(Directory directory,
                                                IndexWriterConfig indexWriterConfig,
                                                SearchServerConfig searchServerConfig) throws IOException {
        return new LuceneNRTIndexProcessor(
            directory, indexWriterConfig,
            searchServerConfig.getChangesBeforeCommit(),
            searchServerConfig.getKeyFieldName()
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
