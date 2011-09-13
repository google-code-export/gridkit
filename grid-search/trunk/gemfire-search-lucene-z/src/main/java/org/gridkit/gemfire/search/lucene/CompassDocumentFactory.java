package org.gridkit.gemfire.search.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.compass.core.Property;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.support.ResourceEnhancer;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalResource;
import org.gridkit.gemfire.search.compass.marshall.GridkitMarshallingContext;
import org.gridkit.gemfire.search.compass.marshall.GridkitMarshallingStrategy;
import org.gridkit.gemfire.search.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CompassDocumentFactory implements DocumentFactory {
    private final static Logger log = LoggerFactory.getLogger(CompassDocumentFactory.class);

    private String keyFieldName;

    private String allProperty;
    private String aliasProperty;
    private String extendedAliasProperty;

    private MarshallingStrategy marshallingStrategy;
    private PropertyNamingStrategy propertyNamingStrategy;

    public CompassDocumentFactory(InternalCompass compass, String keyFieldName) {
        this.keyFieldName = keyFieldName;

        LuceneSearchEngineFactory searchEngineFactory = (LuceneSearchEngineFactory)compass.getSearchEngineFactory();

        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(compass.getSettings());
        SearchEngine searchEngine = searchEngineFactory.openSearchEngine(runtimeSettings);

        this.propertyNamingStrategy = compass.getPropertyNamingStrategy();

        this.marshallingStrategy = new GridkitMarshallingStrategy(
            compass.getMapping(), searchEngine, compass.getConverterLookup(),
            compass.getResourceFactory(), this.propertyNamingStrategy
        );

        this.allProperty = searchEngineFactory.getLuceneSettings().getAllProperty();
        this.aliasProperty = searchEngineFactory.getLuceneSettings().getAliasProperty();
        this.extendedAliasProperty = searchEngineFactory.getLuceneSettings().getExtendedAliasProperty();
    }

    public ObjectDocument createObjectDocument(Object key, Object value) {
        InternalResource resource = (InternalResource)marshallingStrategy.marshall(value);

        ResourceEnhancer.Result result = ResourceEnhancer.enahanceResource(resource);

        removeInternalProperties(result.getDocument());

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

    private void removeInternalProperties(Document document) {
        document.removeField(allProperty);
        document.removeField(aliasProperty);
        document.removeField(extendedAliasProperty);

        Set<String> fieldsToRemove = new HashSet<String>();

        for(Fieldable field : document.getFields())
            if (propertyNamingStrategy.isInternal(field.name()))
                fieldsToRemove.add(field.name());

        for(String fieldName : fieldsToRemove)
            document.removeField(fieldName);
    }
}
