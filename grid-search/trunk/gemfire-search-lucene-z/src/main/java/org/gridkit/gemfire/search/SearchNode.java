package org.gridkit.gemfire.search;

import static org.gridkit.gemfire.search.DemoFactory.*;

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

import static org.gridkit.gemfire.search.DemoFactory.createCache;

public class SearchNode {
    public static void main(String[] args) throws IOException, InterruptedException {
        InternalCompass compass = createCompass();
        Directory directory = createDirectory(compass);
        IndexWriterConfig indexWriterConfig = createIndexWriterConfig(compass);

        LuceneGemfireConfig luceneGemfireConfig = new LuceneGemfireConfig(authorFunctionId);

        LuceneGemfireFactory luceneGemfireFactory = new LuceneGemfireFactory(
            compass, directory, indexWriterConfig, luceneGemfireConfig
        );

        Cache cache = createCache();

        createPartitionedRegion (
            cache, authorRegionName, authorHubName, false
        );

        GatewayHub searchHub = cache.addGatewayHub(authorHubName, -1);
        searchHub.setStartupPolicy(GatewayHub.STARTUP_POLICY_NONE);
        Gateway searchGateway = searchHub.addGateway(authorGatewayName);
        searchGateway.addListener(luceneGemfireFactory.getGatewayListener());
        searchHub.start();

        FunctionService.registerFunction(luceneGemfireFactory.getSearchFunction());

        Thread.sleep(Long.MAX_VALUE);
    }

    private static InternalCompass createCompass() {
        CompassConfiguration configuration = new CompassConfiguration().configure("/compass.cfg.xml");
        return (InternalCompass)configuration.buildCompass();
    }

    private static IndexWriterConfig createIndexWriterConfig(InternalCompass compass) {
        return new IndexWriterConfig(Version.LUCENE_33, LuceneGemfireFactory.getDefaultAnalyzer(compass));
    }

    private static Directory createDirectory(InternalCompass compass) throws IOException {
        Directory directory = new RAMDirectory();

        IndexWriterConfig indexWriterConfig = createIndexWriterConfig(compass);

        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

        indexWriter.commit(); indexWriter.close();

        return directory;
    }
}
