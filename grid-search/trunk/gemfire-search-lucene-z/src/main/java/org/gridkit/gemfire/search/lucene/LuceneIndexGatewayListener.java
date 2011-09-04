package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Operation;
import com.gemstone.gemfire.cache.util.GatewayEvent;
import com.gemstone.gemfire.cache.util.GatewayEventListener;
import org.apache.lucene.document.Field;
import org.compass.core.lucene.engine.transaction.support.ResourceEnhancer;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.spi.InternalResource;
import org.gridkit.gemfire.search.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class LuceneIndexGatewayListener implements GatewayEventListener {
    private static Logger log = LoggerFactory.getLogger(LuceneIndexGatewayListener.class);

    private String keyFieldName;
    private String compassAllProperty;

    private LuceneIndexProcessor indexProcessor;
    private MarshallingStrategy marshallingStrategy;

    public LuceneIndexGatewayListener(MarshallingStrategy marshallingStrategy,
                                      LuceneIndexProcessor indexProcessor,
                                      String keyFieldName, String compassAllProperty) {
        this.marshallingStrategy = marshallingStrategy;
        this.indexProcessor = indexProcessor;

        this.keyFieldName = keyFieldName;
        this.compassAllProperty = compassAllProperty;
    }

    @Override
    public boolean processEvents(List<GatewayEvent> gatewayEvents) {
        for(GatewayEvent event : gatewayEvents) {
            try {
                processEvent(event);
            } catch (IOException e) {
                log.warn("Failed to process event " + event, e);
            }
        }
        return true;
    }

    public void processEvent(GatewayEvent event) throws IOException {
        if (isCreateEvent(event) || isUpdateEvent(event)) {
            InternalResource resource = (InternalResource)marshallingStrategy.marshall(
                    event.getDeserializedValue()
            );
            ResourceEnhancer.Result result = createDocument(resource, event);

            if (isCreateEvent(event))
                indexProcessor.insert(result.getDocument(), result.getAnalyzer());
            else if (isUpdateEvent(event))
                indexProcessor.update(result.getDocument(), result.getAnalyzer());
        } else if (isDestroyEvent(event)) {
            indexProcessor.delete(Serialization.toString(event.getKey()));
        }
    }

    private ResourceEnhancer.Result createDocument(InternalResource resource, GatewayEvent event) throws IOException {
        ResourceEnhancer.Result result = ResourceEnhancer.enahanceResource(resource);

        Field keyField = new Field(keyFieldName, Serialization.toString(event.getKey()),
                                   Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        result.getDocument().add(keyField);

        result.getDocument().removeField(compassAllProperty);

        return result;
    }

    public static boolean isCreateEvent(GatewayEvent event) {
        return Operation.CREATE.equals(event.getOperation());
    }

    public static boolean isUpdateEvent(GatewayEvent event) {
        return Operation.UPDATE.equals(event.getOperation());
    }

    public static boolean isDestroyEvent(GatewayEvent event) {
        return Operation.DESTROY.equals(event.getOperation());
    }

    @Override
    public void close() {}
}
