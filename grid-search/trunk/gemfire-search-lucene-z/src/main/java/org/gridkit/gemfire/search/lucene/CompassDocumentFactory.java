package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.document.Field;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.support.ResourceEnhancer;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalResource;
import org.gridkit.gemfire.search.compass.marshall.GridkitMarshallingStrategy;
import org.gridkit.gemfire.search.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.io.IOException;

public class CompassDocumentFactory implements DocumentFactory {
    private final static Logger log = LoggerFactory.getLogger(CompassDocumentFactory.class);

    private String keyFieldName;
    private String compassAllProperty;

    private MarshallingStrategy marshallingStrategy;

    public CompassDocumentFactory(InternalCompass compass, String keyFieldName) {
        this.keyFieldName = keyFieldName;

        LuceneSearchEngineFactory searchEngineFactory = (LuceneSearchEngineFactory)compass.getSearchEngineFactory();

        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(compass.getSettings());
        SearchEngine searchEngine = searchEngineFactory.openSearchEngine(runtimeSettings);

        this.marshallingStrategy = new GridkitMarshallingStrategy(
            compass.getMapping(), searchEngine, compass.getConverterLookup(),
            compass.getResourceFactory(), compass.getPropertyNamingStrategy()
        );

        this.compassAllProperty = searchEngineFactory.getLuceneSettings().getAllProperty();
    }

    public ObjectDocument createObjectDocument(Object key, Object value) {
        InternalResource resource = (InternalResource)marshallingStrategy.marshall(value);

        ResourceEnhancer.Result result = ResourceEnhancer.enahanceResource(resource);

        result.getDocument().removeField(compassAllProperty);

        try {
            Field keyField = new Field(keyFieldName, Serialization.toString(key),
                                       Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
            result.getDocument().add(keyField);
        } catch (IOException e) {
            log.warn("Failed to serialize object key " + key, e);
            return ObjectDocument.emptyObjectDocument;
        }

        return new ObjectDocument(result.getDocument(), result.getAnalyzer());
    }
}
