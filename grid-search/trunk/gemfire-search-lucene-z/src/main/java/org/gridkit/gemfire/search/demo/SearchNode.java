package org.gridkit.gemfire.search.demo;

import static org.gridkit.gemfire.search.demo.DemoFactory.*;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.util.Gateway;
import com.gemstone.gemfire.cache.util.GatewayHub;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.spi.InternalCompass;
import org.gridkit.gemfire.search.lucene.*;

import java.io.IOException;
import java.util.concurrent.Callable;

import static org.gridkit.gemfire.search.demo.DemoFactory.createCache;

public class SearchNode implements Callable<Void> {
    @Override
    public Void call() throws Exception {
        InternalCompass compass = createCompass();
        Directory directory = createDirectory(compass);
        IndexWriterConfig indexWriterConfig = createIndexWriterConfig(compass);

        SearchServerConfig luceneGemfireConfig = new SearchServerConfig();

        Cache cache = createCache();

        Region authorRegion = createPartitionedRegion (
            cache, authorRegionName, authorHubName, false
        );

        SearchServerFactory luceneGemfireFactory = new SearchServerFactory(
            authorRegion.getFullPath(), compass, directory, indexWriterConfig, luceneGemfireConfig
        );

        GatewayHub searchHub = cache.addGatewayHub(authorHubName, -1);
        searchHub.setStartupPolicy(GatewayHub.STARTUP_POLICY_NONE);
        Gateway searchGateway = searchHub.addGateway(authorGatewayName);
        searchGateway.addListener(luceneGemfireFactory.getGatewayListener());

        FunctionService.registerFunction(IndexDiscoveryFunction.Instance);
        FunctionService.registerFunction(luceneGemfireFactory.getSearchFunction());

        searchHub.start();

        Thread.sleep(Long.MAX_VALUE);

        return null;
    }

    private InternalCompass createCompass() {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");
        return (InternalCompass)configuration.buildCompass();
    }

    private IndexWriterConfig createIndexWriterConfig(InternalCompass compass) {
        return new IndexWriterConfig(Version.LUCENE_33, SearchServerFactory.getDefaultAnalyzer(compass));
    }

    private Directory createDirectory(InternalCompass compass) throws IOException {
        Directory directory = new RAMDirectory();

        IndexWriterConfig indexWriterConfig = createIndexWriterConfig(compass);

        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

        indexWriter.commit(); indexWriter.close();

        return directory;
    }
}
