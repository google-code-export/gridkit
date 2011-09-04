package org.gridkit.gemfire.search.lucene;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;
import org.gridkit.gemfire.search.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LuceneResultCollector<K, V> implements ResultCollector<Serializable, HashMap<K, V>> {
    private static Logger log = LoggerFactory.getLogger(LuceneResultCollector.class);

    private Region<K, V> region;
    private HashMap result = new HashMap<K, V>();

    public LuceneResultCollector(Region<K, V> region) {
        this.region = region;
    }

    @Override
    public void addResult(DistributedMember memberID, Serializable optionKey) {
        if (String.class.isInstance(optionKey) && !LuceneSearchFunction.lastResult.equals(optionKey)) {
            Object objectKey;

            try {
                objectKey = Serialization.toObject((String)optionKey);
            } catch (Exception e) {
                log.warn("Exception while deserializing string key " + optionKey, e);
                return;
            }

            V objectValue = region.get(objectKey);

            if (objectValue != null)
                result.put(objectKey, objectValue);
        } else if (Throwable.class.isInstance(optionKey))
            log.warn("Exception on remote member " + memberID.toString(), (Throwable)optionKey);
    }

    @Override
    public HashMap<K, V> getResult() throws FunctionException {
        return result;
    }

    @Override
    public HashMap<K, V> getResult(long timeout, TimeUnit unit) throws FunctionException, InterruptedException {
        return result;
    }

    @Override
    public void clearResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endResults() {}
}
